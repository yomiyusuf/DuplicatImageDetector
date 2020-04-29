## About

This is an Android app that searches for duplicated images within the **assets** directory 

## Solution
- The first step is to get a list of all the files (paths)
- Each file has to be read and compared with others (This could be an expensive operation depending on the file sizes and number). Instead of loading each file into memory (massive space cost), a far more efficient method would be taking 3 samples (start, middle, and end) from each file. This gives us O(1) space per file and O(n) overall. These samples are then combined and used to generate a fingerprint for the file. This way, we have a constant read time.
- The sample size was set as the file system's block size (the smallest unit that can be read into memory) to maximize the efficiency of the disc read.
- Instead of comparing each file to the others (an n^2 problem), I decided to store the hashes in a hash map as they are generated. 
- The list of files is processed iteratively. Each file has a fingerprint generated in constant time. The fingerprint is stored in the hashmap (Hashmap<fingerprint, listOf(path)>). 
- If the current file's fingerprint is already a key in the hashmap( a O(1) search), the path is added to the list of paths for that fingerprint entry. If the fingerprint doesn't exist in the hashmap, a new entry is created.
- At the end of the process, the hashMap is filtered to get the entries whose listof(path).size > 1. These represent the duplicated files.

This solution can handle thousands of files gracefully. It will also handle files that have more than 2 duplicates even if the extensions were changed.


## Architecture
The app is implemented using the Google-recommended MVVM (Model-view-view model) architecture. This helps achieve good separation of the UI layer from business logic. 
The core of the application is the DuplicateDetector. Its methods handle the search for duplicates. It uses LiveData to wrap its response which is consumed by the ViewModel. The MainActivity in turn, observes this value in the viewModel and displays the images and paths of the duplicateData

## Decisions

 1. Image location - The supplied folder was dropped in the assets directory of the app. This helps meet the following needs: 
- the app can be distributed with the images in-built
- the images folder's hierarchy can be kept intact. This is opposed to using the res/raw folder that can't have sub-directories.
- the assets folder also allows spaces in file names.
2. The UI was designed to be very flexible
- A HorizontalScrollview is used to display duplicates of each file. This way, numerous duplicates can be accommodated.
4. The list of duplicated files is displayed using a RecyclerView. Because a recyclerView only inflates a few items per time, even a very long list of thousands can be efficiently handled.

## Possible improvements
1. Create UI to request images directory selection so the user can pick the desired folder.