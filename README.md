# CA
#The Memory Game
using android studio version 4.2.2
Details description of project
1. Only two Activities are required for the app.
2. The first Activity allows a URL to be specified. Clicking on the Fetch button will extract the first 20 images that it finds on the webpage that the URL points to and display the downloaded images in a grid. A progress-bar should show the number of images downloaded so far with description text (e.g. Downloading 10 of 20 images ...)
3. The user can change the URL in the middle of a download and click on the Fetch button again. The current download would then be aborted and all images in the grid will be cleared. Repeat step 2 based on the new URL specified.
4. Recommended to use https://stocksnap.io as your URL source as the image extraction at that site is straightforward.
5. Once the first 20 images have been downloaded, allow the user to select 6 of them.
6. Once 6 images have been selected, bring the user to the second Activity.
8. The second Activity will take the 6 selected images and use them for the memory game. In the beginning, display 12 placeholders. Then when a placeholder is touched, reveals the image behind that placeholder and wait for the second placeholder to be touched. When the user touched the second placeholder, reveals the image behind that placeholder. If both images are identical, leave both images as they are. If they are different, hide the two images and revert back to display the two placeholders.
  
9. The top left corner should display the matches so far and the top right corner should show a run-up timer.
10.When all images matches, return to the first Activity automatically. User can enter a different URL and download new images to play the game again.

Ideas for interesting features:
• App allows 2 or multi-player mode where the app tracks which
player finishes first and broadcast winner to all participants
• App adds special sound effects/animations when two images match
or mis-match
