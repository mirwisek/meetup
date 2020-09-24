// const functions = require('firebase-functions');
const admin = require("firebase-admin");
const uuid = require('uuid-random');

var serviceAccount = require("./credentials.json");

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://meetup-appp.firebaseio.com"
});


const db = admin.firestore();

const test = async () => {
    let id = uuid()
    let body = {
        title: "Event Invitation",
        body: "You've been invited to an event an other event",
        isSeen: false,
        createdAt: new Date()
    }
    let notification = {}
    notification[id] = body

    let tokens = []

    db.collection("notifications").doc("03000900786").update(notification).catch(e => {
        console.error(e)
    }).then(r => {
        console.log("updated")
    })

    // db.collection("notifications").doc("")
    // try {
    //     await db.runTransaction(async (t) => {
    //
    //         const tokensRef = db.collection("tokens").doc("fcm")
    //         const eventsRef = db.collection("events").doc("OyouvpeJNz1XaVYSGMO1")
    //         const eventDoc = await t.get(eventsRef)
    //         const eventData = eventDoc.data()
    //
    //         const tokenDoc = await t.get(tokensRef)
    //         const tokensData = tokenDoc.data()
    //
    //
    //         eventData.invites.forEach(invite => {
    //             const notificationRef = db.collection("notifications").doc(invite)
    //
    //             t.set(notificationRef, notification)
    //
    //
    //             tokens.push(tokensData[invite])
    //         })
    //
    //     });
    //
    //     console.log(`Transaction success! ${tokens}`);
    // } catch (e) {
    //     console.log('Transaction failure:', e);
    // }

    // console.log("Running events")
    // console.log(context.params)
}

try {
    test();
} catch (e) {
    console.error("error occured", e)
}


