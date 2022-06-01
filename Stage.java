package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Stage class.
 * @author Pauline Tang */
public class Stage implements Serializable {

    /** Staged for addition. */
    private HashMap<String, String> _addition;

    /** Staged for removal. */
    private ArrayList<String> _removal;

    /** Untracked files. */
    private ArrayList<String> _untrackedfiles;

    /** Creates a new stage. */
    public Stage() {
        _addition = new HashMap<String, String>();
        _removal = new ArrayList<String>();
        _untrackedfiles = new ArrayList<String>();
    }

    /** Add the file to the staging area for addition. (Hashmap addition).
     * @param filename name of file.
     * @param blobhash hash of blob object. */
    public void add(String filename, String blobhash) {
        _addition.put(filename, blobhash);
    }

    /** Remove file from the staging area for addition.
     * @param filename name of file. */
    public void removeFromAddition(String filename) {
        _addition.remove(filename);
    }

    /** Add the file to the staging area for removal. (Hashmap removal).
     * @param filename name of file. */
    public void remove(String filename) {
        _removal.add(filename);
    }

    /** Remove the file from the staging area for removal.
     * @param filename name of file. */
    public void removeFromRemoval(String filename) {
        _removal.remove(filename);
    }

    /** Clear the staging area. */
    public void clearStage() {
        _addition.clear();
        _removal.clear();
    }

    /** Serialize the stage. */
    public void saveStage() {
        Utils.writeObject(Main.getStageFile(), this);
    }

    /** Getter method for the Hashmap addition (Staged for addition).
     * @return additions. */
    public HashMap<String, String> getAddition() {
        return _addition;
    }

    /** Getter method for the ArrayList removal (Staged for removal).
     * @return removals. */
    public ArrayList<String> getRemoval() {
        return _removal;
    }

    /** Getter method for the ArrayList untracked files.
     * @param branchname name of branch.
     * @return untracked files. */
    public ArrayList<String> getUntracked(String branchname) {
        _untrackedfiles = new ArrayList<String>();

        Tree t = Main.getTree();

        Branch br = Branch.getBranch(branchname);
        String headc = br.getHeadCommit();
        Commit c = Commit.getCommit(headc);

        List<String> filesinCWD = Utils.plainFilenamesIn(Main.getCWD());
        HashMap<String, String> filesinC = c.getFilesTracked();

        for (String name : filesinCWD) {
            if (!_addition.containsKey(name) || !_removal.contains(name)) {
                if (!filesinC.containsKey(name)) {
                    _untrackedfiles.add(name);
                }
            } else if (_addition.containsKey(name)) {
                File cwdfile = Utils.join(Main.getCWD(), name);
                String cwdcontents = Utils.readContentsAsString(cwdfile);

                String bhash = _addition.get(name);
                File f = Utils.join(Main.getBlobsFolder(), bhash);
                Blob b = Utils.readObject(f, Blob.class);
                String contents = b.getContents();

                if (!cwdcontents.equals(contents)) {
                    _untrackedfiles.add(name);
                }
            } else if (filesinC.containsKey(name)) {
                File cwdfile = Utils.join(Main.getCWD(), name);
                String cwdcontents = Utils.readContentsAsString(cwdfile);

                String bhash = filesinC.get(name);
                File f = Utils.join(Main.getBlobsFolder(), bhash);
                Blob b = Utils.readObject(f, Blob.class);
                String contents = b.getContents();

                if (!cwdcontents.equals(contents)) {
                    _untrackedfiles.add(name);
                }
            }
        }
        return _untrackedfiles;
    }

}
