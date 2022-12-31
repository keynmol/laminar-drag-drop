package laminar.dragdrop

case class DraggableState[T](what: Serialised[T], kind: DraggableState.Kind)

object DraggableState:
  enum Kind:
    case DragStart, Drag, DragEnd
