package net.lagerwey.gash.command;

import net.lagerwey.gash.TreeElement;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.space.Space;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Creates a tree of spaces and remote space uses.
 */
public class TreeCommand implements Command {

    private static final char BEGIN_ELBOW = '\u2514';
    private static final char BEGIN_VERTICAL_AND_RIGHT = '\u251C';
    private static final char VERTICAL = '\u2502';
    private static final char HORIZONTAL = '\u2500';

    @Override
    public void perform(Admin admin, String command, String arguments) {
        List<TreeElement<Space>> tree = new ArrayList<TreeElement<Space>>();
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            for (ProcessingUnitInstance processingUnitInstance : container.getProcessingUnitInstances()) {
                TreeElement<Space> treeElement = new TreeElement<Space>();
                if (processingUnitInstance.isJee()) {
                    treeElement.setKey(processingUnitInstance.getName());
                }
                for (SpaceServiceDetails spaceServiceDetails : processingUnitInstance.getSpaceDetails()) {
                    if (spaceServiceDetails.getSpaceType().equals(SpaceType.EMBEDDED)) {
                        // Embedded space
                        treeElement.setKey(spaceServiceDetails.getName());
                    } else if (spaceServiceDetails.getSpaceType().equals(SpaceType.REMOTE)) {
                        // Remote space
                        treeElement.getChildren().add(new TreeElement<Space>(spaceServiceDetails.getName()));
                    }
                }
                if (!tree.contains(treeElement)) {
                    tree.add(treeElement);
                }
            }
        }

        printElements(tree, 1, false);
    }

    private void printElements(List<TreeElement<Space>> tree, int level, boolean parentIsLastElement) {
        Collections.sort(tree, new Comparator<TreeElement<Space>>() {
            @Override
            public int compare(TreeElement<Space> o1, TreeElement<Space> o2) {
                return o1.getKey().compareToIgnoreCase(o2.getKey());
            }
        });
        for (int i = 0; i < tree.size(); i++) {
            TreeElement<Space> spaceTreeElement = tree.get(i);
            for (int j = 1; j < level; j++) {
                if (parentIsLastElement) {
                    System.out.print(" ");
                } else {
                    System.out.print(VERTICAL);
                }
                System.out.print("   ");
            }
            boolean isLastElement = i == tree.size() - 1;
            if (isLastElement) {
                System.out.print(BEGIN_ELBOW);
            } else {
                System.out.print(BEGIN_VERTICAL_AND_RIGHT);
            }
            System.out.printf("%s%s %s\n", HORIZONTAL, HORIZONTAL, spaceTreeElement.getKey());
            printElements(spaceTreeElement.getChildren(), level + 1, isLastElement);
        }
    }

    private TreeElement<Space> findTreeElement(List<TreeElement<Space>> tree, Space space) {
        for (TreeElement<Space> spaceTreeElement : tree) {
            if (spaceTreeElement.getKey().equals(space.getName())) {
                return spaceTreeElement;
            }
        }
        return null;
    }

    @Override
    public String description() {
        return "Show a tree of spaces.";
    }

    @Override
    public boolean connectionRequired() {
        return true;
    }
}
