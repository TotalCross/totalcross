// Copyright (C) 2004 Trev Quang Nguyen
// Copyright (C) 2005-2013 SuperWaba Ltda. 
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.tree;

import totalcross.util.Vector;

/**
 * This class defines the requirements for an object that can be used as a
 * tree node in a Tree. 
 * 
 */

public class Node extends Vector implements totalcross.util.Comparable {
  /** This node's parent (root node is when the parent node = null). */
  protected Node parent;

  /** The user's object that can be set with anything you want.
   * 
   * The userObject can also be a Control (or Container); in this case, all controls inside of 
   * it are drawn. The control itself is added to the Tree at an invisible location and is painted
   * at the right position. This means that events are somewhat limited. You should change the lineH 
   * to increase the line to the desired control's height.
   * 
   * Here's a sample:
   * <pre>
   class Item extends Container
   {
      String txt;
      public Item(String s) {txt = s;}
  
      public void initUI()
      {
         add(new Check("Here"),LEFT,CENTER,PARENTSIZE+50,PREFERRED);
         add(new Button(txt),AFTER,CENTER,PARENTSIZE+50,PREFERRED);
      }
   }
  
   public void initUI()
   {
      try
      {
         setUIStyle(Settings.Android);
         TreeModel tmodel = new TreeModel();
         Tree tree = new Tree(tmodel);
         tree.setCursorColor(Color.RED);
         tree.setLineHeight(fmH*3/2);
         add(tree,LEFT+50,TOP+50,FILL-50,FILL-50);
         Node root = new Node("Tree");
         tmodel.setRoot(root);
         Node n;
         root.add(n = new Node("Branch1"));
         n.add(new Node(new Item("b1")));
         n.add(new Node(new Item("b2")));         
      }
      catch (Exception ee)
      {
         MessageBox.showException(ee,true);
      }
   }
   * </pre>
   */
  public Object userObject;

  /** The user's int that can be set with anything you want.
   * @since TotalCross 1.25 
   */
  public int userInt; // guich@tc125_30

  /** flag to determine if this node can have children */
  public boolean allowsChildren;

  /** flag to determine if the leaf node has been clicked before */
  public boolean visited;

  /** flag to determine if this node is checked (in multiple-selection trees). */
  public boolean isChecked;

  /** The background color, or -1 to use the default.
   * @since TotalCross 1.2 
   */
  public int backColor = -1; // guich@tc120_13

  /** The foreground color, or -1 to use the default.
   * @since TotalCross 1.2 
   */
  public int foreColor = -1; // guich@tc120_13

  // internal use only
  int level;
  boolean expanded;

  /**
   * Default constructor to create a tree node that has no parent and no children, but which allows children.
   */
  public Node() {
    this("");
  }

  /**
   * Default constructor to create a tree node with no parent, no children, but which allows children, and initializes
   * it with the specified user object.
   *
   * @param userObject
   *           the user object.
   */
  public Node(Object userObject) {
    super(5);
    this.userObject = userObject;
  }

  /**
   * Method to remove newChild from its parent and makes it a child of this node by adding it to the end of this node's
   * child vector.
   *
   * @param newChild
   *           the new child node to add to the end of the child vector.
   */
  public void add(Node newChild) {
    if (newChild.parent != null) {
      newChild.removeFromParent();
    }
    newChild.parent = this;
    addElement(newChild);
  }

  /**
   * Method to create and return a vector that traverses the subtree rooted at this node in breadth-first order.
   *
   * @return the children vector of this node.
   */
  public Vector breadthFirstVector() {
    Vector v = new Vector(count);
    breadthFirst(v, this);
    return v;
  }

  /**
   * Method used by breathFirstVector() to create the breath first vector.
   *
   * @param v
   *           the vector to hold tree nodes.
   * @param node
   *           the node to traverse
   */
  private void breadthFirst(Vector v, Node node) {
    v.addElement(node);
    for (int i = 0, n = node.count; i < n; i++) {
      breadthFirst(v, (Node) node.items[i]);
    }
  }

  /**
   * Method to returns the child in this node's child array that immediately follows aChild, which must be a child of
   * this node; otherwise, retrun null.
   *
   * @return the child node that immediately follows aChild node.
   */
  public Node getChildAfter(Node aChild) {
    int pos = indexOf(aChild);
    if (pos >= 0 && pos < count - 1) {
      return (Node) items[pos + 1];
    }
    return null;
  }

  /**
   * Method to returns the child in this node's child array that immediately precedes aChild, which must be a child of
   * this node; otherwise, retrun null.
   *
   * @return the child node that immediately precede aChild node.
   */
  public Node getChildBefore(Node aChild) {
    int pos = indexOf(aChild);
    if (pos > 0) {
      return (Node) items[pos - 1];
    }
    return null;
  }

  /**
   * Method to return this node's first child.
   *
   * @return this node's first child, or null if there's none.
   */
  public Node getFirstChild() {
    return count == 0 ? null : (Node) items[0];
  }

  /**
   * Method to return this node's last child.
   *
   * @return this node's last child, or null if there's none.
   */
  public Node getLastChild() {
    return count == 0 ? null : (Node) items[count - 1];
  }

  /**
   * Method to return the number of levels above this node -- the distance from the root to this node.
   *
   * @return the number of levels above this node -- the distance from the root to this node
   */
  public int getLevel() {
    int level = -1;
    for (Node node = this; node != null; node = node.parent) {
      level++;
    }
    return level;
  }

  /**
   * Method to return the node name of this node.
   *
   * @return the node name of this node.
   */
  public String getNodeName() {
    return userObject == null ? "" : userObject.toString();
  }

  /**
   * Method to return the next sibling of this node in the parent's children array. Returns null if this node has no
   * parent or is the parent's last child. This method performs a linear search that is O(n) where n is the number of
   * children
   *
   * @return the next sibling of this node in the parent's children array. Returns null if this node has no parent or
   *         is the parent's last child.
   */
  public Node getNextSibling() {
    return parent != null ? parent.getChildAfter(this) : null;
  }

  /**
   * Method to return the previous sibling of this node in the parent's children array. Returns null if this node has
   * no parent or is the parent's first child. This method performs a linear search that is O(n) where n is the number
   * of children
   *
   * @return the previous sibling of this node in the parent's children array. Returns null if this node has no parent
   *         or is the parent's in the tree.
   */
  public Node getPreviousSibling() {
    return parent != null ? parent.getChildBefore(this) : null;
  }

  /**
   * Method to return this node's parent or null if this node has no parent.
   */
  public Node getParent() {
    return parent;
  }

  /**
   * Method to return the path from the root, to get to this node.
   *
   * @return the path from the root, to get to this node.
   */
  public Node[] getPath() {
    Vector v = new Vector();
    pathFromRootToNode(v, this);

    Node p[] = new Node[v.size()];
    v.copyInto(p);
    return p;
  }

  /**
   * Method to builds the parents of node up to and including the root node, where the original node is the last
   * element in the returned array.
   *
   * @return the path from this node to the root node, including the root node.
   */
  protected Node[] getPathToRoot() {
    Vector v = new Vector();
    pathFromNodeToRoot(v, this);

    Node p[] = new Node[v.size()];
    v.copyInto(p);
    return p;
  }

  /**
   * Method to get the path from the root to the specified node.
   *
   * @param v
   *           the vector to hold the path.
   * @param node
   *           the specified node.
   */
  private void pathFromRootToNode(Vector v, Node node) {
    if (node != null) {
      pathFromRootToNode(v, node.parent);
      v.addElement(node);
    }
  }

  /**
   * Method to get the path from the specified node to the root node.
   *
   * @param v
   *           the vector to hold the path.
   * @param node
   *           the specified node.
   */
  private void pathFromNodeToRoot(Vector v, Node node) {
    if (node != null) {
      v.addElement(node);
      pathFromNodeToRoot(v, node.parent);
    }
  }

  /**
   * Method to return the root of the tree that contains this node.
   *
   * @return the root of the tree that contains this node.
   */
  public Node getRoot() {
    Node node = this;
    while (!node.isRoot()) {
      node = node.parent;
    }
    return node;
  }

  /**
   * Method to return the user object path, from the root, to get to this node.
   *
   * @return the user object path, from the root, to get to this node.
   */
  public Object[] getUserObjectPath() {
    Node nodes[] = getPath();
    Object obj[] = new Object[nodes.length];
    for (int i = 0; i < obj.length; i++) {
      obj[i] = nodes[i].userObject;
    }
    return obj;
  }

  /**
   * Method to removes newChild from its present parent (if it has a parent), sets the child's parent to this node, and
   * then adds the child to this node's child array at index childIndex. If childINdex is out of bound, newChild will
   * be inserted at the end of the children vector
   *
   * @param newChild
   *           the new child to remove from this subtree and add to this node children vector at the specified index.
   * @param childIndex
   *           the position in the children vector to insert newChild.
   * @return the child index.
   */
  public int insert(Node newChild, int childIndex) {
    if (newChild.parent != null) {
      newChild.removeFromParent();
    }

    try {
      insertElementAt(newChild, childIndex);
      newChild.parent = this;
      return childIndex < 0 || childIndex >= count ? indexOf(newChild) : childIndex;
    } catch (Exception e) {
      addElement(newChild);
      newChild.parent = this;
      return count - 1;
    }
  }

  /**
   * Method to return true if this node has no children.
   *
   * @return true if this node has no children.
   */
  public boolean isLeaf() {
    return count == 0;
  }

  /**
   * Method to return true if this node has no children.
   *
   * @return true if this node has no children.
   */
  public boolean isLeaf(boolean useAllowsChildren) {
    return count == 0 && (!useAllowsChildren || !this.allowsChildren);
  }

  /**
   * Method to return true if this node is a root. Root node is node that has a null parent node.
   */
  public boolean isRoot() {
    return parent == null;
  }

  /**
   * Method to return true if aNode is a child of this node.
   *
   * @param aNode
   *           the node to deterimine if it's a shild of this node.
   * @return true if aNode is a child of this node.
   */
  public boolean isNodeChild(Node aNode) {
    if (aNode.parent == this) {
      for (int i = 0; i < count; i++) {
        if (aNode == items[i]) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Method to return true if anotherNode is a sibling of (has the same parent as) this node.
   *
   * @return true if anotherNode is a sibling of (has the same parent as) this node.
   */
  public boolean isNodeSibling(Node anotherNode) {
    return parent != null && parent == anotherNode.parent;
  }

  /**
   * Method to remove the child at the specified index from this node's children and sets that node's parent to null.
   *
   * @param childIndex
   *           the index of the this node's children to be removed
   */
  public void remove(int childIndex) {
    if (childIndex > -1 && childIndex < count) {
      ((Node) items[childIndex]).parent = null;
      removeElementAt(childIndex);
    }
  }

  /**
   * Method to remove aChild from this node's child array, giving it a null parent.
   *
   * @param aChild
   *           the child node to remove.
   */
  public void remove(Node aChild) {
    if (aChild.parent == this) {
      removeElement(aChild);
      aChild.parent = null;
    }
  }

  /**
   * Method to remove all of this node's children, setting their parents to null.
   * If you don't want to set their parents to null, call removeAllElements instead.
   */
  public void removeAllChildren() {
    for (int i = 0, n = count; i < n; i++) {
      ((Node) items[i]).parent = null;
    }
    removeAllElements();
  }

  /**
   * Method to remove the subtree rooted at this node from the tree, giving this node a null parent.
   */
  public void removeFromParent() {
    parent.remove(this);
  }

  /**
   * Method to sets this node's parent to newParent but does not change the parent's child array.
   *
   * @param parent
   *           the newParent node
   */
  public void setParent(Node parent) {
    this.parent = parent;
  }

  /**
   * Method to return the result of sending toString() to this node's user object, or null if this node has no user
   * object.
   *
   * @return the result of sending toString() to this node's user object, or null if this node has no user object.
   */
  @Override
  public String toString() {
    return getNodeName();
  }

  @Override
  public int compareTo(Object o) {
    Node other = (Node) o;
    if (other.userObject instanceof totalcross.util.Comparable) {
      return ((totalcross.util.Comparable) userObject).compareTo((totalcross.util.Comparable) other.userObject);
    }
    return this.equals(other) ? 0 : -1;
  }

  @Override
  public boolean equals(Object o) {
    return o.equals(userObject);
  }
}
