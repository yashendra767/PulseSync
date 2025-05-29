/* eslint-disable */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();

exports.syncWithNIC = functions.pubsub.schedule('every 5 minutes').onRun(async (context) => {
  const db = admin.firestore();

  try {
    const hospitalID = "DEL123"; // Change per hospital

    //  Fetch beds
    const bedsSnapshot = await db.collection('beds').get();
    const bedStatus = [];
    for (const doc of bedsSnapshot.docs) {
      const bed = doc.data();

      // Optional: check if occupied, get patient ID
      let patientID = null;
      if (bed.status === "Occupied") {
        const admissionSnap = await db.collection("admissions")
          .where("bedId", "==", doc.id)
          .where("status", "==", "active")
          .limit(1)
          .get();

        if (!admissionSnap.empty) {
          patientID = admissionSnap.docs[0].data().patientId;
        }
      }

      bedStatus.push({
        bedID: doc.id,
        status: bed.status,
        patientID: patientID || null,
      });
    }

    //  Fetch inventory
    const inventorySnapshot = await db.collection('inventory').get();
    const inventoryStatus = inventorySnapshot.docs.map(doc => {
      const item = doc.data();
      return {
        itemName: item.itemName,
        quantity: item.quantity,
      };
    });

    //  Build NIC Payload
    const payload = {
      hospitalID,
      bedStatus,
      inventoryStatus,
    };

    //  Send to NIC API
    const response = await axios.post('https://api.nic.in/your-endpoint', payload, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${functions.config().nic.api_key}`, // Secure this later
      },
    });

    console.log("✅ NIC sync successful:", response.data);

  } catch (error) {
    console.error("❌ NIC sync failed:", error.message);
  }
});
