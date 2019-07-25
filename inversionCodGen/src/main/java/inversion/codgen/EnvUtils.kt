package inversion.codgen

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.tools.Diagnostic

fun ProcessingEnvironment.log(msg: String) {
    if (options.containsKey("debug")) {
        messager.printMessage(Diagnostic.Kind.NOTE, msg)
    }
}

fun ProcessingEnvironment.warn(msg: String, element: Element, annotation: AnnotationMirror? = null) {
    messager.printMessage(Diagnostic.Kind.WARNING, msg, element, annotation)
}

fun ProcessingEnvironment.error(msg: String, element: Element, annotation: AnnotationMirror? = null) {
    messager.printMessage(Diagnostic.Kind.ERROR, msg, element, annotation)
}

fun ProcessingEnvironment.fatalError(msg: String) {
    messager.printMessage(Diagnostic.Kind.ERROR, "FATAL ERROR: $msg")
}

fun ProcessingEnvironment.getPackageName(it: Element) =
    elementUtils.getPackageOf(it).toString()
