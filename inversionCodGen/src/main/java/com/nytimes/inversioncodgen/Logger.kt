package com.nytimes.inversioncodgen

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.tools.Diagnostic

fun ProcessingEnvironment.log(msg: String) {
    if (options.containsKey("debug")) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg)
    }
}

fun ProcessingEnvironment.error(msg: String, element: Element, annotation: AnnotationMirror?) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, element, annotation)
}

fun ProcessingEnvironment.fatalError(msg: String) {
    messager.printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: $msg")
}