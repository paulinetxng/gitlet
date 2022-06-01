package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;

/** From spec:
 * A commit, therefore, will consist of a log message, timestamp,
 * a mapping of file names to blob references, a parent reference,
 * and (for merges) a second parent reference.
 * @author Pauline Tang */
public class Commit implements Serializable {

    /** Message of commit. */
    private String _message;

    /** Timestamp of commit. */
    private String _timestamp;

    /** Files tracked in commit. */
    private HashMap<String, String> _filesTracked;

    /** Parent commit. */
    private String _parent;

    /** Hash of commit. */
    private String _identifier;

    /** Branch of commit. */
    private String _branch;

    /** Creates a new commit object with specified parameters.
     * Timestamp of commit gets recorded.
     * @param message commit message.
     * @param parenthash hash of parent commit.
     * @param branchname branch of commit. */
    public Commit(String message, String parenthash, String branchname) {
        _message = message;
        _parent = parenthash;
        SimpleDateFormat formatter =
                new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
        if (parenthash == null) {
            _timestamp = formatter.format(new Date(0));
        } else {
            _timestamp = formatter.format(new Date());
        }
        _identifier = Utils.sha1(message, _timestamp);
        _branch = branchname;

        _filesTracked = new HashMap<String, String>();
        updateFilesTracked();
    }

    /** Serialize the commit. */
    public void saveCommit() {
        File f = Utils.join(Main.getCommitsFolder(), _identifier);
        Utils.writeObject(f, this);
    }

    /** Copy over the tracked files from the parent commit.
     * Also insert in new files from the staging area. */
    public void updateFilesTracked() {
        Stage s = Main.getStage();
        if (_parent != null) {
            _filesTracked = getCommit(_parent).getFilesTracked();
            for (String filename : s.getAddition().keySet()) {
                _filesTracked.put(filename, s.getAddition().get(filename));
            }
            ArrayList<String> stagedforremoval = s.getRemoval();
            for (String filename : stagedforremoval) {
                _filesTracked.remove(filename);
            }
        }
    }

    /** Return the Commit object from the hashcode of the commit.
     * @param commithash commit hashcode. */
    public static Commit getCommit(String commithash) {
        if (commithash == null) {
            return null;
        }
        File f = Utils.join(Main.getCommitsFolder(), commithash);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = Utils.readObject(f, Commit.class);
        return c;
    }

    /** Getter method for the Hashmap of the files this Commit is tracking.
     * @return filestracked. */
    public HashMap<String, String> getFilesTracked() {
        return _filesTracked;
    }

    /** Getter method for the message of this commit.
     * @return message. */
    public String getMessage() {
        return _message;
    }

    /** Getter method for the timestamp of this commit as a Date.
     * @return timestamp. */
    public String getTimestamp() {
        return _timestamp;
    }

    /** Getter method for the parent of this commit as a Commit.
     * @return parent commit. */
    public Commit getParent() {
        return getCommit(_parent);
    }

    /** Getter method for the Commit's hash/identifier.
     * @return identifier. */
    public String getIdentifier() {
        return _identifier;
    }

    /** Getter method for the Commit's branch.
     * @return branch. */
    public String getBranch() {
        return _branch;
    }

    /** Getter method for the hash of it's parent commit.
     * @return parenthash. */
    public String getParentHash() {
        return _parent;
    }


}
