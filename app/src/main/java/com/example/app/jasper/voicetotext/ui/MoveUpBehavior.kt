package com.example.app.jasper.voicetotext.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import kotlin.math.min


//Use this class only if you want to slide up the UI whenever the seekbar appears.
class MoveUpwardBehavior : CoordinatorLayout.Behavior<View?> {
    constructor() : super() {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}

    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        val translationY = min(0f, ViewCompat.getTranslationY(dependency) - dependency.height)
        ViewCompat.setTranslationY(child, translationY)
        return true
    }

    //you need this when you swipe the snackbar
    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        ViewCompat.animate(child).translationY(0f).start()
    }
}