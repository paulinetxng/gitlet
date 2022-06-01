# Gitlet Design Document

**Name**: Pauline Tang

# Classes and Data Structures
## Commit
A class that will create and save commits

**Fields**
1. String _message: commit message
2. Date _timestamp: date of commit
3. String _timestampString: date of commit as a string
4. Commit _parent: parent of commit
5. String _identifier: hashcode of commit
6. Hashmap filesTracked: Contains all the files that are in this commit.

## Stage
A class that will handle stage addition/removal

**Fields**
1. Hashmap addition: Contains files staged for addition
2. Hashmap removal: Contains files staged for removal

## Blob
A class that will handle blob objects

**Fields**
1. String _filename: the file that is the blob
2. String _identifier: the file's hashcode

## Tree
A class that will include all the commits into a "tree"

**Fields**
1. Hashmap commitTreeMap: hashmap of all the commits ever made
2. Commit _currcommit: the latest commit

# Algorithms
## Commit class
1. Commit(String message, Commit parent): Class constructor. Records the message, parent, and timestamp of the commit. Creates a HashMap of the files in this commit (copying from parent and updating from stage). Also records its own unique identifier (from its message and timestamp).
2. updateFilesTracked(): Adds files from the stage into the hashmap filesTracked.
3. saveCommit(): serializes the commit object
4. getMessage(): returns commit message as a String
5. getParent(): returns parent commit as a Commit
6. getTimestamp(): returns timestamp as a Date
7. getTimestampString(): returns timestamp as a String
8. getIdentifier(): returns identifier as a String

## Stage class
1. Stage(): Class constructor. Creates a hashmap for addition and a hashmap for removal.
2. add(String filename, Blob blob): Adds the filename and its blob object into the staging area for addition (HashMap addition).
3. removeFromAddition(String filename): Removes a file from the staging area for addition.
4. remove(String filename, Blob blob): Adds the filename and its blob object into the staging area for removal
5. removeFromRemoval(String filename): Removes the file from the staging area for removal.
6. clearStage(): Clears the hashmaps for addition and removal; clearing the stage.
7. saveStage(): serializes the Stage.
8. getAddition(): returns addition hashmap.
9. getRemoval(): returns removal hashmap.

## Blob class
1. Blob(String filename, String content): Class constructor. Records the blob's filename and its content as a string (created from Utils.readContentAsString)
2. saveBlob(): serializes the blob.
3. getFilename(): returns the filename as a String.
4. getIdentifier(): returns the identifier (content as a string from constructor).

## Tree class
1. Tree(): Class constructor. Creates a hashmap that will keep track of all commits made.
2. addToTree(Commit c): adds a commit to the tree (hashmap commitTreeMap). Updates the current commit to the one just added
3. getCurrCommit(): returns the current commit as a Commit.
4. getCommitTreeMap(): returns the hashmap of the commit tree.
5. saveTree(): serializes the Tree.

# Persistence
1. Before running any of the commands, call setUpPersistance() in Main (inspo from lab11). Will create any directories/files that haven't been created yet.
2. init: 
   1. Creates a new stage, then serializes it with Stage.saveStage(). 
   2. Creates a new tree.
   3. Creates initial commit, then serializes it with Commit.saveCommit(). 
   4. Add the initial commit to the tree, then serialize the tree.
3. add: 
   1. Deserialize the stage and tree with getStage() and getTree(). 
      1. (The stage and tree are both files, not directories.)
   2. Read the filename with Utils.readContentsAsString and write it to a blob object.
   3. Add the new blob object to the staging area for addition.
   4. Get the previous commit from the tree and if the file in the commit is the same as the file to be added, remove it from the stage.
   5. Serialize the stage with Stage.saveStage().
4. commit:
   1. Deserialize the stage and tree with getStage() and getTree().
      1. (The stage and tree are both files, not directories.)
   2. Get previous commit from the tree and create a new commit with the previous one as its parent.
   3. Clear the stage after the commit tracks all files that were staged for addition.
   4. Add the commit to the tree.
   5. Serialize the stage with Stage.saveStage().
   6. Serialize the commit with Commit.saveCommit().
   7. Serialize the tree with Tree.saveTree().

