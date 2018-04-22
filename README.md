# Solaria (Android Application)
Solaria Hydroponics was our idea for Innovation Labs 2018.

Solaria offers an automated hydroponic agriculture kit that's easy to use, stays connected to the Internet and can be monitored/controlled remotely, at any time.

It consists of a hardware part (Raspberry Pi, Arduino and different sensors), a Web Application and an Android App. I was the one responsible for developing the Android App.

You have access to real-time status of the plants in your systems (you can have as many systems as you wish), as well as the option to turn on/off the artificial light for each one.

We have also implemented warning notifications, in case that sensors read data which is over/under certain limits (specified by the user).

We designed the product with ease of use in mind. Before monitoring a system, you have to pair it with the Solaria App. To do this, you can either use Bluetooth Low Energy for a one-tap pairing solution, or you can enter the system's Access Code manually (a code which is located on a sticker, on the actual Arduino chip).

Since we needed real-time monitoring and interaction, Firebase was the best choice for us. From the Firebase platform, we used: Firebase Realtime Database (in order to store the crucial pieces of data the app depends on), Firebase Google Authentication (to implement an easy and straightforward login flow) and, last but not least, Firebase Cloud Messaging (to send notifications containing warnings, in case that sensor values go off limits).

Credits to [Marcus](https://github.com/MarcusGitAccount) and [Jacharcus](https://github.com/Jacharcus), who are responsible for working on the other parts of Solaria.
