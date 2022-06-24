import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * <p>
 * You must add code for the 1 member and 4 methods specified below.
 * <p>
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
  private DecTreeNode root;
  //ordered list of class labels
  private List<String> labels;
  //ordered list of attributes
  private List<String> attributes;
  //map to ordered discrete values taken by attributes
  private Map<String, List<String>> attributeValues;

  /**
   * Answers static questions about decision trees.
   */
  DecisionTreeImpl() {
    // no code necessary this is void purposefully
  }

  /**
   * Build a decision tree given only a training set.
   *
   * @param train: the training set
   */
  DecisionTreeImpl(DataSet train) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // build the tree from the training data set.
    this.root = buildTree(train, this.attributes, null, null);
  }

  private DecTreeNode buildTree(DataSet examples, List<String> leftAttributes,
                                DataSet parentExamples, String parentVal) {
    // stopping criteria
    if (examples.instances.size() == 0) return genTerminalNode(parentExamples.pluralityValue(), parentVal);
    else if (examples.allLabeledSame()) return genTerminalNode(examples.instances.get(0).label, parentVal);
    else if (leftAttributes.size() == 0) return genTerminalNode(examples.pluralityValue(), parentVal);
    else {
      // find most important in dataset, from the current left attributes.
      String mostImportantAttribute = examples.getMostImportantAttribute(leftAttributes);
      DecTreeNode toReturn = new DecTreeNode(null, mostImportantAttribute, parentVal, false);
      // clone, just without the currently used one.
      List<String> attributesWithoutMostImportant = new ArrayList<String>(leftAttributes);
      attributesWithoutMostImportant.remove(mostImportantAttribute);
      // build children nodes by attribute values
      for (String attributeValue : attributeValues.get(mostImportantAttribute)) {
        DataSet nextExamples = examples.getFiltered(mostImportantAttribute, attributeValue);
        toReturn.addChild(buildTree(nextExamples, attributesWithoutMostImportant, examples, attributeValue));
      }
      return toReturn;
    }
  }

  private DecTreeNode genTerminalNode(String label, String parentVal) {
    return new DecTreeNode(label, null, parentVal, true);
  }

  @Override
  public String classify(Instance instance) {
    // using iteration instead of recursion. Still the same.
    DecTreeNode currNode = this.root;
    while (!currNode.terminal) {
      String currAttribute = currNode.attribute;
      String attributeValInInstance = instance.attributes.get(this.attributes.indexOf(currAttribute));
      for (DecTreeNode c : currNode.children) {
        if (c.parentAttributeValue.equals(attributeValInInstance)) {
          currNode = c;
          break;
        }
      }
    }
    return currNode.label;
  }

  @Override
  public void rootInfoGain(DataSet train) {
    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    double globalB = train.B();
    // TODO: add code here
    for (String attribute : this.attributes) {
      double currGain = globalB - train.attributeRemainder(attribute);
      System.out.printf("%s %.5f\n", attribute, currGain);
    }
  }

  @Override
  public void printAccuracy(DataSet test) {
    int instanceCount = test.instances.size();
    double rightCount = 0;
    for (Instance i : test.instances) {
      if (this.classify(i).equals(i.label))
        rightCount += 1;
    }
    System.out.printf("%.5f\n", rightCount / instanceCount);
  }

  /**
   * Build a decision tree given a training set then prune it using a tuning set.
   * ONLY for extra credits
   *
   * @param train: the training set
   * @param tune:  the tuning set
   */
  DecisionTreeImpl(DataSet train, DataSet tune) {

    this.labels = train.labels;
    this.attributes = train.attributes;
    this.attributeValues = train.attributeValues;
    // TODO: add code here
    // only for extra credits
  }

  @Override
  /**
   * Print the decision tree in the specified format
   */
  public void print() {
    printTreeNode(root, null, 0);
  }

  /**
   * Prints the subtree of the node with each line prefixed by 4 * k spaces.
   */
  public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < k; i++) {
      sb.append("    ");
    }
    String value;
    if (parent == null) {
      value = "ROOT";
    } else {
      int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
      value = attributeValues.get(parent.attribute).get(attributeValueIndex);
    }
    sb.append(value);
    if (p.terminal) {
      sb.append(" (" + p.label + ")");
      System.out.println(sb.toString());
    } else {
      sb.append(" {" + p.attribute + "?}");
      System.out.println(sb.toString());
      for (DecTreeNode child : p.children) {
        printTreeNode(child, p, k + 1);
      }
    }
  }

  /**
   * Helper function to get the index of the label in labels list
   */
  private int getLabelIndex(String label) {
    for (int i = 0; i < this.labels.size(); i++) {
      if (label.equals(this.labels.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attribute in attributes list
   */
  private int getAttributeIndex(String attr) {
    for (int i = 0; i < this.attributes.size(); i++) {
      if (attr.equals(this.attributes.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
   */
  private int getAttributeValueIndex(String attr, String value) {
    for (int i = 0; i < attributeValues.get(attr).size(); i++) {
      if (value.equals(attributeValues.get(attr).get(i))) {
        return i;
      }
    }
    return -1;
  }
}
