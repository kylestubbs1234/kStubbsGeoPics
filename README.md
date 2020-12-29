# kStubbsGeoPics
In this project, I created a social media app that allows users to tag each post with a specific location.

**Login and registration:**
The user is initially prompted to login or register an account. Both of these are performed through Firebase authentication.

**Home page:**
The main page for this app consists of markers that show all of the locations of the posts by the users. The initial camera view is set to the location of UCSB.

**Creating a post:**
On app startup, the user is prompted to allow camera and storage permissions. Both of these need to be enabled for the user to take a picture and store it externally in a file.

Once the photo is taken, the user must post a caption and select a location on the map for the data to be stored in Firebase.

**Viewing a post:**
Clicking the list button in the top right corner to view the posts in a RecyclerView, which is sorted by the distance from the current location. The user can also click directly on a marker to take the user to that location in the list. In this view, the user can like the posts, and the total number of likes will be displayed for each post.
