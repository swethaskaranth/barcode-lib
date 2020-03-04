package com.pharmeasy.barcode

/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */

open class Event<out T>(private val content:T) {
    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again
     */

    fun getContentIfNotHandled():T?{
        return if (hasBeenHandled){
            null
        } else{
            hasBeenHandled=true
            content
        }
    }

    /**
     * Returns the content if its already been handled
     */
    fun peekContent():T =content
}