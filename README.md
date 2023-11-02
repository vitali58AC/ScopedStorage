# Scoped storage

Used: Media Store API, Jetpack Compose, Retrofit, Glide, Coroutine.

**Application description:**

All videos from the device saved in shared external storage are displayed on the main screen.
To do this, the necessary permissions are requested at startup.
Videos can be downloaded using Retrofit.

Videos downloaded by the application, as well as videos from other applications, can be deleted,
marked as favorites, and added to cart. Uses both the classic use of registerForActivityResult
and rememberLauncherForActivityResult Compose to request the required permissions from the user.
