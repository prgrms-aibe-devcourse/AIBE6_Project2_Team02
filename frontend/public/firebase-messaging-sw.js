importScripts(
  'https://www.gstatic.com/firebasejs/10.12.2/firebase-app-compat.js',
)
importScripts(
  'https://www.gstatic.com/firebasejs/10.12.2/firebase-messaging-compat.js',
)

firebase.initializeApp({
  apiKey: 'AIzaSyAIe7cXp9N92m8MSWXjpLEenT0DvJ-90cA',
  projectId: 'aibe6-fcm-notifications',
  messagingSenderId: '798136002896',
  appId: '1:798136002896:web:cd293297ed245ee0a32455',
})

const messaging = firebase.messaging()
