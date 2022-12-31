package laminar.dragdrop

case class DropZoneState[T](item: Serialised[T], kind: DropZoneState.Kind)

object DropZoneState:
  enum Kind:
    case DragEnter, DragOver, Drop, DragLeave
