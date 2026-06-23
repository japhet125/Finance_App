const { setGlobalOptions } = require("firebase-functions");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { defineSecret } = require("firebase-functions/params");
const logger = require("firebase-functions/logger");
const sgMail = require("@sendgrid/mail");

setGlobalOptions({ maxInstances: 10 });

const SENDGRID_API_KEY = defineSecret("SENDGRID_API_KEY");

exports.sendQueuedEmail = onDocumentCreated(
  {
    document: "email_requests/{emailId}",
    secrets: [SENDGRID_API_KEY],
  },
  async (event) => {
    const snap = event.data;

    if (!snap) {
      return;
    }

    const emailData = snap.data();

    if (emailData.status !== "pending") {
      logger.info("Email request skipped because status is not pending.");
      return;
    }

    if (!emailData.email || !emailData.subject || !emailData.message) {
      await snap.ref.set(
        {
          status: "failed",
          error: "Missing email, subject, or message",
          processedAt: Date.now(),
        },
        { merge: true }
      );

      return;
    }

    sgMail.setApiKey(SENDGRID_API_KEY.value());

    const msg = {
      to: emailData.email,
      from: "guibrilramde@gmail.com",
      subject: emailData.subject,
      text: emailData.message,
    };

    try {
      await sgMail.send(msg);

      await snap.ref.set(
        {
          status: "sent",
          sentAt: Date.now(),
          error: "",
        },
        { merge: true }
      );

      logger.info("Email sent successfully", {
        emailId: event.params.emailId,
        to: emailData.email,
      });
    } catch (error) {
      await snap.ref.set(
        {
          status: "failed",
          error: error.response?.body?.errors?.[0]?.message || error.message,
          processedAt: Date.now(),
        },
        { merge: true }
      );

      logger.error("Email failed", error);
    }
  }
);
