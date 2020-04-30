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

/**
 * This class holds the tree structure that the Tree class used to render the tree widget. Note: you should
 * use this class to mofidy the tree after the root node is setted to this model; otherwise, the tree will 
 * reflect the change visually; a faster option for bulk inserts is to use the node's add/insert methods 
 * and then do a reload.
 * @see #reload
 * @see Node#add
 * @see Node#insert
 * @see #addNode(Node, Node)
 * @see #insertNode(Node, Node, int)
 */
public class TreeModel {
  private Tree tree;
  private Node root;

  /** flag used to determine if a node is a leaf or a folder. */
  public boolean allowsChildren;

  /**
   * Constructor to create a tree model with the specified root node and with the specified allowsChildren flag.
   */
  public TreeModel(Node root, boolean allowsChildren) {
    this.root = (root != null) ? root : new Node("");
    this.allowsChildren = allowsChildren;
  }

  /**
   * Constructor to create a tree model with the specified root node and with allowsChildren is true.
   */
  public TreeModel(Node root) {
    this(root, true);
  }

  /**
   * Constructor to create an empty tree model with allowsChildren is true.
   */
  public TreeModel() {
    this(true);
  }

  /**
   * Constructor to create an empty tree model that use allowsChildren to determine the leaf node, if and only if
   * allowsChildren is true.
   *
   * @param allowsChildren
   *           true to use allowwsChildren to determine a leaf node.
   */
  public TreeModel(boolean allowsChildren) {
    this(null, allowsChildren);
  }

  /**
   * Method to set the tree (For internal use only) This method register the tree to this model, so when the user add,
   * delete, or modify a node, the tree view will be notify and updated.
   *
   * @param tree
   *           the tree (view) that is associated with this model.
   */
  public void setTree(Tree tree) {
    this.tree = tree;
  }

  /**
   * Method to clear this model.
   */
  public void clear() {
    root = new Node("");
    tree.setModel(this);
  }

  /**
   * Method to notify the tree to reload.
   */
  public void reload() {
    if (tree != null) {
      tree.reload();
    }
  }

  /**
   * Method to return the root node of this tree model.
   *
   * @return the root node of this tree model.
   */
  public Node getRoot() {
    return root;
  }

  /**
   * Method to set the root node of this tree model and notify the tree to reload the tree.
   *
   * @param root
   *           the new root node of this tree model.
   */
  public void setRoot(Node root) {
    this.root = root;
    reload();
  }

  /**
   * Method to insert a node to the parent node at the specified position. This method will notify the associated tree
   * to display the node, if the parent node is expanded. If the index out of range, the new node will be inserted at
   * the end of the parent node children vector (consider, in this case, using the addNode method, which is faster).
   *
   * @param parent
   *           the parent node of the node to insert.
   * @param newNode
   *           the new node to insert into this tree model.
   * @param index
   *           the index to insert the node into
   */
  public void insertNode(Node parent, Node newNode, int index) {
    if (parent == null || newNode == null || parent.isLeaf(allowsChildren)) {
      return; // cannot insert into a leaf node
    }

    index = parent.insert(newNode, index);
    if (tree != null) {
      tree.nodeInserted(parent, newNode, index);
    }
  }

  /**
   * Method to add a node to the parent node at the specified position. This method will notify the associated tree
   * to display the node, if the parent node is expanded. This is fastest than insertNode.
   *
   * @param parent
   *           the parent node of the node to insert.
   * @param newNode
   *           the new node to insert into this tree model.
   */
  public void addNode(Node parent, Node newNode) {
    if (parent == null || newNode == null || parent.isLeaf(allowsChildren)) {
      return; // cannot insert into a leaf node
    }

    parent.add(newNode);
    if (tree != null) {
      tree.nodeInserted(parent, newNode, parent.size());
    }
  }

  /**
   * Method to remove a node from the tree. This method will notify the tree to collapse the tree.
   *
   * @param parent
   *           the parent node of the node to remove.
   * @param node
   *           the node to remove from this tree model.
   */
  public void removeNode(Node parent, Node node) {
    // cannot delete root node
    if (parent == null || node == null) {
      return;
    }

    parent.remove(node);
    if (tree != null) {
      tree.nodeRemoved(node);
    }
  }

  /**
   * Method to modify a node userObject and notify the tree of the changes.
   *
   * @param node
   *           the node to modify.
   * @param userObject
   *           the new user object.
   */
  public void modifyNode(Node node, Object userObject) {
    if (node == null) {
      return;
    }

    node.userObject = userObject;
    tree.nodeModified(node);
  }
}
