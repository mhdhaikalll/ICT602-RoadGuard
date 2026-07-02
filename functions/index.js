const { initializeApp } = require('firebase-admin/app');
const { getFirestore } = require('firebase-admin/firestore');
const { onDocumentCreated } = require('firebase-functions/v2/firestore');
const { getMessaging } = require('firebase-admin/messaging');
const geofire = require('geofire-common');

initializeApp();

const GEOHASH_PRECISION = 9;

exports.sendProximityAlert = onDocumentCreated(
  {
    document: 'reports/{reportId}',
    region: 'asia-southeast1'
  },
  async (event) => {
    const report = event.data.data();
    if (!report) {
      console.log('No data found');
      return;
    }

    const reportGeohash = report.geohash;
    if (!reportGeohash) {
      console.log('No geohash in report, skipping');
      return;
    }

    // Decode geohash to get latitude/longitude
    const center = geofire.geohash.decode(reportGeohash);
    const radiusInM = 5000;
    const bounds = geofire.geohashQueryBounds(center, radiusInM);

    const db = getFirestore();
    const promises = [];
    for (const b of bounds) {
      const q = db.collection('users')
        .where('geohash', '>=', b[0])
        .where('geohash', '<=', b[1])
        .where('alertsEnabled', '==', true)
        .get();
      promises.push(q);
    }

    const snapshots = await Promise.all(promises);
    const tokens = new Set();

    snapshots.forEach(snapshot => {
      snapshot.forEach(doc => {
        const user = doc.data();
        if (!user.geohash || !user.fcmToken) return;

        const userCenter = geofire.geohash.decode(user.geohash);
        const distance = distanceInKm(center, userCenter);
        const userRadius = user.notificationRadiusKm || 5;
        if (distance <= userRadius) {
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
        latitude: String(report.latitude),
        longitude: String(report.longitude),
        severity: report.severity
      }
    };

    const messaging = getMessaging();
    const response = await messaging.sendEachForMulticast({
      tokens: tokensArray,
      ...payload
    });

    console.log('Successfully sent notifications:', response.successCount);
    // Optional: clean invalid tokens
    response.responses.forEach((resp, idx) => {
      if (!resp.success) {
        const code = resp.error?.code;
        if (code === 'messaging/invalid-registration-token' ||
            code === 'messaging/registration-token-not-registered') {
          // TODO: remove invalid token from Firestore
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

function distanceInKm(coord1, coord2) {
  const R = 6371;
  const dLat = toRad(coord2.latitude - coord1.latitude);
  const dLon = toRad(coord2.longitude - coord1.longitude);
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(coord1.latitude)) * Math.cos(toRad(coord2.latitude)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function toRad(degrees) {
  return degrees * Math.PI / 180;
}