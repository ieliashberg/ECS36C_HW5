package edu.ucdavis.cs.ecs036c

import org.junit.jupiter.api.Test

/*
 * This also checks that the tree is AVL balanced, the height is
 * properly computed, and that the tree is well-formed.
 */
fun <T: Comparable <T>> AVLBinaryTreeNode<T>.wellFormed(
    initialValue: T,
    visited: MutableSet<AVLBinaryTreeNode<T>> = mutableSetOf<AVLBinaryTreeNode<T>>()): Boolean {
    val isRoot = visited.size == 0

    if (this in visited) return false
    if (balance > 1 || balance < -1) return false
    visited.add(this)
    val children =  (left?.wellFormed(initialValue, visited) ?: true) &&
            (right?.wellFormed(initialValue, visited) ?: true)
    if (!children) return false
    val lastBalance = balance
    val lastHeight = height
    val lastSize = size
    // Force update to force updating the size.  It shouldn't actualyl
    // have changed here.
    this.left = this.left
    this.right = this.right
    if (height != lastHeight || size != lastSize || lastBalance != balance) return false
    if (isRoot) {
        var a = initialValue
        for (b in this.inOrderTraversal()) {
            if (a > b) return false;
            a = b
        }
    }
    return true
}


class AVLBinaryTreeNodeTest {

    /*
     * This is a very useful test to set a breakpoint on and step through and
     * make sure the tree is well-formed if you are having trouble with the other test.
     */
    @Test
    fun testAVL(){
        val test = toOrderedTree(0,1,2,3,4,5,6,7,8,9)
        assert(test.size == 10)
        for(x in 0..<10){
            assert(x in test)
            test.remove(x)
            assert(test.size == 10 - (x+1))
        }
        assert(test.size == 0)
    }


    @Test
    fun testTreeCode(){
        for(x in 0..<100){
            for(y in 0..<10){
                val testData : Array<Int?> = arrayOfNulls(x)
                for(z in 0..<x){
                    testData[z] = z
                }
                @Suppress("UNCHECKED_CAST")
                testData as Array<Int>
                testData.shuffle()
                val testTree = toOrderedTree(*testData)
                var i = 0
                assert(testTree.size == x)
                assert(testTree.root?.wellFormed(-1) ?: true)
                for (data in testTree){
                    assert(i == data)
                    assert(i in testTree)
                    assert((i + x) !in testTree)
                    i++

                }
                testData.shuffle()
                for (data in testData){
                    assert(data in testTree)
                    testTree.remove(data)
                    assert(data !in testTree)
                    assert(testTree.root?.wellFormed(-1) ?: true)
                }
                assert(testTree.size == 0)
                for (data in testData){
                    assert(data !in testTree)
                }
                testData.shuffle()
                for (data in testData){
                    assert(data !in testTree)
                    testTree.insert(data)
                    assert(data in testTree)
                    assert(testTree.root?.wellFormed(-1) ?: true)
                }
            }
        }

    }

}