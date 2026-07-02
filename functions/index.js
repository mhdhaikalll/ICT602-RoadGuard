const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const geofire = require('geofire-common');

initializeApp();

exports.sendProximityAlert = onDocumentCreated(
  {
    document: 'reports/{reportId}',
    region: 'asia-southeast1'            // or 'us-central1'
  },
  async (event) => {
    const report = event.data.data();
    if (!report) {
      console.log('No data found');
      return;
    }

    const reportLat = report.latitude;
    const reportLng = report.longitude;
    if (reportLat == null || reportLng == null) {
      console.log('Report missing coordinates');
      return;
    }

    const center = [reportLat, reportLng];
    const radiusInKm = 5;   // search up to 5 km (max user radius)
    const radiusInM = radiusInKm * 1000;   // geohashQueryBounds expects metres
    const bounds = geofire.geohashQueryBounds(center, radiusInM);

    const db = getFirestore();
    const promises = bounds.map(([start, end]) =>
      db.collection('users')
        .where('alertsEnabled', '==', true)
        .orderBy('geohash')
        .startAt(start)
        .endAt(end)
        .get()
    );

    const snapshots = await Promise.all(promises);
    const tokens = new Set();

    snapshots.forEach(snapshot => {
      snapshot.forEach(doc => {
        const user = doc.data();
        if (!user.fcmToken) return;

        const userLat = user.lastLatitude;   // must match Firestore field name
        const userLng = user.lastLongitude;
        if (userLat == null || userLng == null) return;

        const userCenter = [userLat, userLng];
        const distanceInKm = geofire.distanceBetween(center, userCenter);   // already in km
        const userRadius = user.notificationRadiusKm || 5;

        if (distanceInKm <= userRadius) {
          tokens.add(user.fcmToken);
        }
      });
    });

    if (tokens.size === 0) {
      console.log('No tokens to send');
      return;
    }

    const tokensArray = Array.from(tokens);
    const payload = {
      notification: {
        title: '⚠️ Road damage reported near you',
        body: `${severityLabel(report.severity)} damage reported nearby. Tap for details.`
      },
      data: {
        reportId: event.params.reportId,
        latitude: String(reportLat),
        longitude: String(reportLng),
        severity: report.severity
      }
    };

    const messaging = getMessaging();
    const response = await messaging.sendEachForMulticast({
      tokens: tokensArray,
      ...payload
    });

    console.log('Successfully sent notifications:', response.successCount);
    response.responses.forEach((resp) => {
      if (!resp.success) {
        const code = resp.error?.code;
        if (code === 'messaging/invalid-registration-token' ||
            code === 'messaging/registration-token-not-registered') {
          // TODO: optionally clean up expired token
        }
      }
    });
  }
);

function severityLabel(severity) {
  switch (severity) {
    case 'high': return 'Severe';
    case 'medium': return 'Moderate';
    default: return 'Minor';
  }
}