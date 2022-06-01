package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Pauline Tang
 */
public class Main {

    /** Current Working Directory. */
    private static File _cwd = new File(System.getProperty("user.dir"));

    /** Metadata folder. */

    /** Gitlet directory. */
    private static File _gitletdir = Utils.join(_cwd, ".gitlet");

    /** Stage file. */
    private static File _stage = Utils.join(_gitletdir, "stage");

    /** Commits folder. */
    private static File _commits = Utils.join(_gitletdir, "commits");

    /** Branches folder. */
    private static File _branches = Utils.join(_gitletdir, "branches");

    /** Blobs folder. */
    private static File _blobs = Utils.join(_gitletdir, "blobs");

    /** Tree File. */
    private static File _tree = Utils.join(_gitletdir, "tree");

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        if (!args[0].equals("init") && !_gitletdir.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            init();
            break;
        case "add":
            add(args[1]);
            break;
        case "commit":
            commit(args[1]);
            break;
        case "rm":
            rm(args[1]);
            break;
        case "checkout":
            if (args.length == 3) {
                checkout(args[2]);
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                checkout(args[1], args[3]);
            } else {
                checkoutBranch(args[1]);
            }
            break;
        case "log":
            log();
            break;
        case "global-log":
            globalLog();
            break;
        case "find":
            find(args[1]);
            break;
        case "status":
            status();
            break;
        case "branch":
            branch(args[1]);
            break;
        case "rm-branch":
            rmBranch(args[1]);
            break;
        case "reset":
            reset(args[1]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }


    /** Creates a new Gitlet version-control system in the current
     * working directory. Starts with the initial commit "initial commit." */
    public static void init() {
        if (_gitletdir.exists()) {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
            System.exit(0);
        }

        _gitletdir.mkdir();
        _commits.mkdir();
        _branches.mkdir();
        _blobs.mkdir();

        try {
            _stage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            _tree.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        Stage s = new Stage();
        s.saveStage();

        Tree t = new Tree();

        Commit initialcommit = new Commit("initial commit", null, "master");
        initialcommit.saveCommit();

        t.addToTree(initialcommit);
        t.addBranch("master", initialcommit.getIdentifier());

        Branch br = new Branch("master", initialcommit.getIdentifier());
        br.saveBranch();

        t.saveTree();
    }

    /** Adds a copy of the file to the staging area. Staging an already staged
     * file will overwrite the previously staged file's contents. If the file
     * to be added is identical to the file in the previous commit, do not
     * add to the staging area and remove it from the staging area if it's
     * already present.
     * @param filename name of file to be added. */
    public static void add(String filename) {
        Stage s = getStage();
        Tree t = getTree();

        File f = Utils.join(_cwd, filename);

        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String contents = Utils.readContentsAsString(f);
        Blob b = new Blob(filename, contents);

        ArrayList<String> srem = s.getRemoval();

        if (srem.contains(filename)) {
            s.removeFromRemoval(filename);
        } else {
            s.add(filename, b.getIdentifier());

            String headbr = t.getHeadBranch();
            Branch br = Branch.getBranch(headbr);
            String headchash = br.getHeadCommit();
            Commit headc = Commit.getCommit(headchash);

            HashMap<String, String> filesInCommit = headc.getFilesTracked();
            for (String name : filesInCommit.keySet()) {
                if (filesInCommit.get(name).equals(b.getIdentifier())) {
                    s.removeFromAddition(filename);
                }
            }
        }

        b.saveBlob();
        s.saveStage();
    }

    /** Saves a snapshot of files in the current commit and staging area,
     * creating a new commit.
     * @param message message of commit. */
    public static void commit(String message) {
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Stage s = getStage();
        Tree t = getTree();

        if (s.getAddition().isEmpty() && s.getRemoval().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        String headbr = t.getHeadBranch();
        Branch br = Branch.getBranch(headbr);
        String headchash = br.getHeadCommit();

        Commit c = new Commit(message, headchash, headbr);
        t.addToTree(c);

        br.changeHead(c.getIdentifier());

        s.clearStage();

        br.saveBranch();
        s.saveStage();
        c.saveCommit();
        t.saveTree();
    }

    /** Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for
     * removal and remove the file from the working directory if
     * the user has not already done so (do not remove it unless
     * it is tracked in the current commit).
     * @param filename name of file. */
    public static void rm(String filename) {
        Stage s = getStage();
        Tree t = getTree();
        Commit c = t.getHeadCommit();

        HashMap<String, String> sadd = s.getAddition();

        if (!sadd.containsKey(filename)
                && !c.getFilesTracked().containsKey(filename)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        if (sadd.containsKey(filename)) {
            s.removeFromAddition(filename);
        }

        if (c.getFilesTracked().containsKey(filename)) {
            s.remove(filename);
            Utils.restrictedDelete(filename);
        }

        s.saveStage();
    }

    /** Takes the version of the file as it exists in the head commit,
     * the front of the current branch, and puts it in the working
     * directory, overwriting the version of the file that's already there
     * if there is one. The new version of the file is not staged.
     * @param filename name of file. */
    public static void checkout(String filename) {
        Tree t = getTree();
        Commit c = t.getHeadCommit();

        File cwdfile = Utils.join(_cwd, filename);

        HashMap<String, String> filesinc = c.getFilesTracked();

        if (!filesinc.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String bhash = c.getFilesTracked().get(filename);
        File f = Utils.join(_blobs, bhash);

        Blob b = Utils.readObject(f, Blob.class);
        String contents = b.getContents();

        Utils.writeContents(cwdfile, contents);
    }

    /** Takes the version of the file as it exists in the commit
     * with the given id, and puts it in the working directory,
     * overwriting the version of the file that's already there
     * if there is one.The new version of the file is not staged.
     * @param commitID id of commit.
     * @param filename name of file. */
    public static void checkout(String commitID, String filename) {
        Tree t = getTree();
        HashMap<String, String> allcommits = t.getCommitTreeMap();

        String fullid = commitID;

        int len = commitID.length();
        for (String id : allcommits.keySet()) {
            if (commitID.equals(id.substring(0, len))) {
                fullid = id;
            }
        }

        Commit c = Commit.getCommit(fullid);

        File cwdfile = Utils.join(_cwd, filename);

        HashMap<String, String> filesinc = c.getFilesTracked();

        if (!filesinc.containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String bhash = c.getFilesTracked().get(filename);
        File f = Utils.join(_blobs, bhash);

        Blob b = Utils.readObject(f, Blob.class);
        String contents = b.getContents();

        Utils.writeContents(cwdfile, contents);
    }

    /** Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist. Also, at the
     * end of this command, the given branch will now be considered the
     * current branch (HEAD). Any files that are tracked in the current
     * branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is
     * the current branch.
     * @param branchname name of branch. */
    public static void checkoutBranch(String branchname) {
        Tree t = getTree();
        Stage s = getStage();
        Branch br = Branch.getBranch(branchname);

        if (t.getHeadBranch().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String chash = br.getHeadCommit();
        Commit c = Commit.getCommit(chash);
        HashMap<String, String> filesinc = c.getFilesTracked();

        ArrayList<String> untracked = s.getUntracked(branchname);
        if (!untracked.isEmpty()) {
            for (String filename : untracked) {
                File f = Utils.join(_cwd, filename);
                Utils.restrictedDelete(f);
            }
        }

        for (String filename : filesinc.keySet()) {
            File cwdfile = Utils.join(_cwd, filename);

            String bhash = filesinc.get(filename);
            File f = Utils.join(_blobs, bhash);

            Blob b = Utils.readObject(f, Blob.class);
            String contents = b.getContents();

            Utils.writeContents(cwdfile, contents);
        }

        t.changeBranch(branchname);
        s.clearStage();

        t.saveTree();
        s.saveStage();
    }

    /** Starting at the current head commit, display information about each
     * commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second
     * parents found in merge commits. (In regular Git, this is what you
     * get with git log --first-parent). This set of commit nodes is called
     * the commit's history. For every node in this history, the information
     * it should display is the commit id, the time the commit was made,
     * and the commit message. */
    public static void log() {
        Tree t = getTree();

        Commit currcommit = t.getHeadCommit();
        while (currcommit != null) {
            String id = currcommit.getIdentifier();
            String date = currcommit.getTimestamp();
            String m = currcommit.getMessage();

            System.out.println("===");
            System.out.println("commit " + id);
            System.out.println("Date: " + date + " -0800");
            System.out.println(m);

            if (currcommit.getParent() != null) {
                System.out.println();
            }

            currcommit = currcommit.getParent();
        }
    }

    /** Like log, except displays information about all commits ever made.*/
    public static void globalLog() {
        List<String> allcommits = Utils.plainFilenamesIn(_commits);
        int num = allcommits.size();
        for (int i = 0; i < num; i++) {
            Commit c = Commit.getCommit(allcommits.get(i));

            String commitid = allcommits.get(i);
            String date = c.getTimestamp();
            String m = c.getMessage();

            System.out.println("===");
            System.out.println("commit " + commitid);
            System.out.println("Date: " + date + " -0800");
            System.out.println(m);

            if (i != (num - 1)) {
                System.out.println();
            }
        }
    }

    /** Prints out the ids of all commits that have the given commit
     * message, one per line. If there are multiple such commits,
     * it prints the ids out on separate lines.
     * @param message message of commit. */
    public static void find(String message) {
        ArrayList<String> commitswithmessage = new ArrayList<String>();

        Tree t = getTree();
        HashMap<String, String> ctmap = t.getCommitTreeMap();

        for (String chash : ctmap.keySet()) {
            Commit c = Commit.getCommit(chash);
            String cmess = c.getMessage();
            if (cmess.equals(message)) {
                commitswithmessage.add(chash);
            }
        }

        for (String s : commitswithmessage) {
            System.out.println(s);
        }

        if (commitswithmessage.isEmpty()) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** Displays what branches currently exist, and marks the current
     * branch with a *. Also displays what files have been staged for
     * addition or removal. */
    public static void status() {
        Tree t = getTree();
        Stage s = getStage();
        String headb = t.getHeadBranch();
        HashMap<String, String> sadd = s.getAddition();
        ArrayList<String> srem = s.getRemoval();

        Branch br = Branch.getBranch(headb);
        String headc = br.getHeadCommit();
        Commit c = Commit.getCommit(headc);

        HashMap<String, String> filesinc = c.getFilesTracked();

        System.out.println("=== Branches ===");
        List<String> bs = Utils.plainFilenamesIn(_branches);
        for (String b : bs) {
            String p = "";
            if (b.equals(headb)) {
                p = "*";
            }
            System.out.println(p + b);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        ArrayList<String> addarr = new ArrayList<String>(sadd.keySet());
        Collections.sort(addarr);
        for (String filename : addarr) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        Collections.sort(srem);
        for (String filename : srem) {
            System.out.println(filename);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        statusMod();
        System.out.println();

        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = s.getUntracked(headb);
        for (String filename : untracked) {
            if (!sadd.containsKey(filename)) {
                System.out.println(filename);
            }
        }

    }

    /** Status modifications. */
    public static void statusMod() {
        Tree t = getTree();
        Stage s = getStage();
        String headb = t.getHeadBranch();
        HashMap<String, String> sadd = s.getAddition();
        ArrayList<String> srem = s.getRemoval();

        Branch br = Branch.getBranch(headb);
        String headc = br.getHeadCommit();
        Commit c = Commit.getCommit(headc);

        HashMap<String, String> filesinc = c.getFilesTracked();
        List<String> cwdfiles = Utils.plainFilenamesIn(_cwd);

        for (String filename : cwdfiles) {
            File cwdfile = Utils.join(_cwd, filename);
            String cwdcontents = Utils.readContentsAsString(cwdfile);

            if (filesinc.containsKey(filename)) {
                String blobhash = filesinc.get(filename);
                File bfile = Utils.join(_blobs, blobhash);
                Blob b = Utils.readObject(bfile, Blob.class);
                String bcont = b.getContents();

                if (!cwdcontents.equals(bcont) && !sadd.containsKey(filename)) {
                    System.out.println(filename + " (modified)");
                }
            }
        }

        for (String filec : filesinc.keySet()) {
            File f = Utils.join(_cwd, filec);
            if (!f.exists() && !srem.contains(filec)) {
                System.out.println(filec + " (deleted)");
            }
        }
    }

    /** Creates a new branch with the given name, and points it at the
     * current head node. A branch is nothing more than a name for a
     * reference (a SHA-1 identifier) to a commit node. This command does
     * NOT immediately switch to the newly created branch (just as in real Git)
     * Before you ever call branch, your code should be running with a default
     * branch called "master".
     * @param branchname name of branch. */
    public static void branch(String branchname) {
        File f = Utils.join(_branches, branchname);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        Tree t = getTree();
        Commit c = t.getHeadCommit();
        String chash = c.getIdentifier();

        t.addBranch(branchname, chash);

        Branch br = new Branch(branchname, chash);

        br.saveBranch();
        t.saveTree();
    }

    /** Deletes the branch with the given name. This only means to delete the
     * pointer associated with the branch; it does not mean to delete all
     * commits that were created under the branch, or anything like that.
     * @param branchname name of branch. */
    public static void rmBranch(String branchname) {
        Tree t = getTree();

        if (t.getHeadBranch().equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        File f = Utils.join(_branches, branchname);
        if (!f.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        f.delete();

        t.getAllBranches().remove(branchname);
        t.saveTree();
    }

    /** Checks out all the files tracked by the given commit. Removes tracked
     * files that are not present in that commit. Also moves the current
     * branch's head to that commit node. See the intro for an example of what
     * happens to the head pointer after using reset. The [commit id] may be
     * abbreviated as for checkout. The staging area is cleared. The command is
     * essentially checkout of an arbitrary commit that also changes the
     * current branch head.
     * @param commitid hash of commit. */
    public static void reset(String commitid) {
        Tree t = getTree();
        Stage s = getStage();

        String bname = t.getHeadBranch();
        Branch b = Branch.getBranch(bname);

        HashMap<String, String> allcommits = t.getCommitTreeMap();
        String fullid = commitid;

        int len = commitid.length();

        for (String id : allcommits.keySet()) {
            if (commitid.equals(id.substring(0, len))) {
                fullid = id;
            }
        }

        Commit c = Commit.getCommit(fullid);
        HashMap<String, String> filesinc = c.getFilesTracked();

        List<String> cwdfiles = Utils.plainFilenamesIn(_cwd);

        for (String filename : cwdfiles) {
            File f = Utils.join(_cwd, filename);
            if (!filesinc.containsKey(filename)) {
                f.delete();
            }
        }

        for (String filename : filesinc.keySet()) {
            checkout(commitid, filename);
        }

        b.changeHead(fullid);
        s.clearStage();

        t.saveTree();
        b.saveBranch();
        s.saveStage();
    }

    /** Deserializes the staging area.
     * @return stage. */
    public static Stage getStage() {
        Stage s = Utils.readObject(_stage, Stage.class);
        return s;
    }

    /** Deserializes the tree.
     * @return tree. */
    public static Tree getTree() {
        Tree t = Utils.readObject(_tree, Tree.class);
        return t;
    }

    /** Getter for CWD.
     * @return CWD. */
    public static File getCWD() {
        return _cwd;
    }

    /** Getter for Gitlet directory.
     * @return gitlet directory. */
    public static File getGitletDir() {
        return _gitletdir;
    }

    /** Getter for Stage file.
     * @return stage file. */
    public static File getStageFile() {
        return _stage;
    }

    /** Getter for Commits folder.
     * @return commits folder. */
    public static File getCommitsFolder() {
        return _commits;
    }

    /** Getter for Branches folder.
     * @return branches folder. */
    public static File getBranchesFolder() {
        return _branches;
    }

    /** Getter for Blobs folder.
     * @return blobs folder. */
    public static File getBlobsFolder() {
        return _blobs;
    }

    /** Getter for Tree File.
     * @return tree file. */
    public static File getTreeFile() {
        return _tree;
    }


}
