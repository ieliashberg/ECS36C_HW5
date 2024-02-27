package edu.ucdavis.cs.ecs036c
import kotlin.math.abs

/*
 * This is the class for the node for an AVL balanced binary
 * tree.  In particular there are some nice features here:
 *
 * We have a private variable height and private internal variables
 * for left and right, with public getting/setting functions that
 * should update the private height whenever the left or right are
 * updated.  This should update the internal height whenever the
 * left or right subtree are assigned.
 *
 * Since the recursive insertion/deletion functions will do the
 * reassignment automatically, this indicates a need to update
 * the internal height.
 */

class AVLBinaryTreeNode<T>(val data: T) {
    /*
     * We have the left/right/height values as private internal variables.
     *
     * Even in your code you don't want to access these directly except
     * for internalHeight when updating the height of the node.
     */
    private var leftInternal: AVLBinaryTreeNode<T>? = null
    private var rightInternal: AVLBinaryTreeNode<T>? = null
    private var internalHeight: Int = 1
    private var internalSize: Int = 1

    /*
     * This is an example of a secondary constructor.  The primary
     * constructor sets both leftInternal and rightInternal to
     * null and the height to 1.  This constructor will first automatically
     * call the base constructor and then it should set left/right
     */
    constructor(data: T,
                leftSubtree : AVLBinaryTreeNode<T>?,
                rightSubtree: AVLBinaryTreeNode<T>?) : this(data){
        left = leftSubtree
        right = rightSubtree
        calculateHeightAndSize()
    }

    /*
     * And this is an example of a smart getter/setter.
     * You should have the setter not only set the internal
     * value for the node but also update the height and size (of which you
     * probably want to define a separate function since you need
     * to update height and size for both left and right updating
     */
    var left : AVLBinaryTreeNode<T>?
        get() = leftInternal
        set(value) {
            leftInternal = value
            calculateHeightAndSize()
        }

    var right : AVLBinaryTreeNode<T>?
        get() = rightInternal
        set(value) {
            rightInternal = value
            calculateHeightAndSize()
        }

    fun calculateHeightAndSize() {
        internalHeight = if((right?.height ?: 0) > (left?.height ?: 0)) {
            (right?.height ?: 0) + 1
        } else {
            (left?.height ?: 0) + 1
        }
        internalSize = (right?.size ?: 0) + (left?.size ?: 0) + 1
    }



    val height : Int
        get() = internalHeight

    val size: Int
        get() = internalSize

    val balance: Int
        get() = (right?.height ?: 0) - (left?.height ?: 0)

    /*
     * The first of the AVL rotations.  IF you only call
     * it when the tree is unbalanced you should be OK
     * without checking.  If you don't you can expect
     * null pointer exceptions.
     */
    fun rotateLeft() : AVLBinaryTreeNode<T> {
        val oldTop = this
        val newTop = oldTop.right
        oldTop.right = newTop!!.left
        newTop.left = oldTop
        return newTop
    }

    /*
     * And the other AVL rotation.
     */
    fun rotateRight() : AVLBinaryTreeNode<T> {
        val oldTop = this
        val newTop = oldTop.left
        oldTop.left = newTop!!.right
        newTop.right = oldTop
        return newTop
    }

    /*
     * And this is the AVL rebalancing function.  It will
     * return A new node, either this node if no rebalancing
     * is necessary or the new root of this subtree if this
     * node needs rebalancing.
     */
    fun rebalance(): AVLBinaryTreeNode<T> {
        if (balance > 1) {
            if((right?.balance ?: 0) < 0) { //if right child is left heavy
                right = right!!.rotateRight()
            }
            return rotateLeft()
        } else if (balance < -1) {
            if((left?.balance ?: 0) > 0) {
                left = left!!.rotateLeft()
            }
            return rotateRight()
        }
        return this
    }


    /*
     * This is the ToString function.  Feel free to add in additional
     * debugging information (height, etc) to this.
     */
    fun toStringInternal(previouslyPrinted: MutableSet<AVLBinaryTreeNode<T>>): String {
        if (this in previouslyPrinted) {
            return "ERROR:ALREADY_VISITED{Data: $data}"
        }
        previouslyPrinted.add(this)
        var res = "("
        if (left != null) {
            res += left?.toStringInternal(previouslyPrinted) + " "
        }
        res += data.toString()
        if (right != null) {
            res += " " + right?.toStringInternal(previouslyPrinted)
        }
        return res + ")"
    }

    override fun toString() = toStringInternal(mutableSetOf<AVLBinaryTreeNode<T>>())


    /*
     * This is the iterative (stack based) in-order traversal that
     * returns a sequence.  We use this design so we can do for loops and the
     * like nice and painlessly, which is not so easy to do on the recursive
     * version since Kotlin doesn't have a python-esque yieldFrom we'd
     * have to manually subcompose things.  Far more efficient to just use
     * an explicit stack.
     */
    fun inOrderTraversal(): Sequence<T> = sequence {
        val workStack = ArrayDeque<AVLBinaryTreeNode<T>>()
        var currentNode: AVLBinaryTreeNode<T>? = this@AVLBinaryTreeNode
        while (!workStack.isEmpty() || currentNode != null) {
            // The logic:  We examine the current node.  IF there
            // is stuff to the left, we push ourselves onto the stack
            // and update the current node
            if (currentNode != null) {
                workStack.addLast(currentNode)
                currentNode = currentNode.left
            } else {
                currentNode = workStack.removeLast()
                yield(currentNode.data)
                currentNode = currentNode.right
            }
        }
    }

    /*
     * The other traversals are not needed for this project
     * but we keep them around for potential utility.
     */
    fun preOrderTraversal(): Sequence<T> = sequence {
        val workStack = ArrayDeque<AVLBinaryTreeNode<T>>()
        workStack.addLast(this@AVLBinaryTreeNode)
        while (!workStack.isEmpty()) {
            val currentNode = workStack.removeLast()
            yield(currentNode.data)
            if(currentNode.right != null){
                workStack.addLast(currentNode.right!!)
            }
            if(currentNode.left != null){
                workStack.addLast(currentNode.left!!)
            }
        }
    }

    // Post order is a bit trickier...
    // We have our current node and our stack, and we check to see if the
    // right subtree is on the top of the stack.
    fun postOrderTraversal(): Sequence<T> = sequence {
        val workStack = ArrayDeque<AVLBinaryTreeNode<T>>()
        var current: AVLBinaryTreeNode<T>? = this@AVLBinaryTreeNode
        while (!workStack.isEmpty() || current != null) {
            if (current == null) {
                current = workStack.removeLast()
                if (!workStack.isEmpty() &&
                    current.right == workStack.get(workStack.lastIndex)
                ) {
                    val tmp = current
                    current = workStack.removeLast()
                    workStack.addLast(tmp)
                } else {
                    yield(current.data)
                    current = null
                }
            } else {
                if(current.right != null){
                    workStack.addLast(current.right!!)
                }
                workStack.addLast(current)
                current = current.left
            }
        }
    }

    fun levelOrderTraversal(): Sequence<T> = sequence {
        val workQueue = ArrayDeque<AVLBinaryTreeNode<T>>()
        workQueue.addLast(this@AVLBinaryTreeNode)
        while (!workQueue.isEmpty()) {
            val current = workQueue.removeFirst()
            yield(current.data)
            if (current.left != null) {
                workQueue.addLast(current.left!!)
            }
            if (current.right != null) {
                workQueue.addLast(current.right!!)
            }
        }
    }



    operator fun iterator() = inOrderTraversal().iterator()
}


class OrderedAVLTree<T: Comparable<T>> {
    var root : AVLBinaryTreeNode<T>? = null

    val size : Int
        get() = root?.size ?: 0

    operator fun iterator() = root?.inOrderTraversal()?.iterator() ?: emptySequence<T>().iterator()

    override fun toString() = root?.toString() ?: "NULL"

    /*
     * Hint, you are allowed to start with sample code from the public
     * archive for these functions...
     */
    fun insert(data: T) {
        fun insertInternal(node: AVLBinaryTreeNode<T>?): AVLBinaryTreeNode<T> {
            if (node == null) {
                return AVLBinaryTreeNode(data, null, null)
            } else if(data < node.data) {
                node.left = insertInternal(node.left)
            } else if(data > node.data){
                node.right = insertInternal(node.right)
            }
            return node.rebalance()
        }
        root = insertInternal(root)
    }

    operator fun contains(data: T): Boolean {
        var current = root
        while (current != null) {
            current = when {
                data < current.data -> current.left
                data > current.data -> current.right
                else -> return true
            }
        }
        return false
    }

    /*
     * Removal logic:
     *
     * If the node has no children: it just gets replaced with null.
     * If the node only has left or right children, it gets replaced
     * with left or right.
     *
     * If the node has BOTH a left and right child, replace the data
     * by finding the smallest item on the right child, deleting THAT node
     * and REPLACING the data in the current node with that node.
     */
    fun remove(data: T){
        if(data !in this) return
        fun removeSmallest(at: AVLBinaryTreeNode<T>): Pair<AVLBinaryTreeNode<T>?, T> {
            if(at.left == null) return Pair(at.right, at.data)
            val (tree, newData) = removeSmallest(at.left!!)
            at.left = tree
            return Pair(at.rebalance(), newData)
        }
        fun removeInternal(at: AVLBinaryTreeNode<T>): AVLBinaryTreeNode<T>?{
            if (at.data == data) {
                if(at.left == null ) return at.right
                else if (at.right == null) return at.left
                else {
                    val (right, newData) = removeSmallest(at.right!!)
                    return AVLBinaryTreeNode(newData, at.left, right).rebalance()
                }
            }
            else if (data < at.data) at.left = removeInternal(at.left!!)
            else at.right = removeInternal(at.right!!)
            return at.rebalance()
        }
        root = removeInternal(root!!)
    }
}

fun <T: Comparable<T>>toOrderedTree(vararg data : T) : OrderedAVLTree<T> {
    val retVal = OrderedAVLTree<T>()
    for(element in data){
        retVal.insert(element)
    }
    return retVal
}