package laminar.dragdrop

import com.raquo.airstream.core.EventStream
import org.scalajs.dom.DragEvent
import com.raquo.laminar.modifiers.EventListener
import com.raquo.laminar.api.L.*

trait Draggable[T]:
  def events: EventStream[DraggableState[T]]
  def binders: Seq[Modifier[Element]]

  def isDragged: Signal[Boolean] =
    events
      .map(_.kind != DraggableState.Kind.DragEnd)
      .startWith(false)
end Draggable

object Draggable:
  def apply[T](value: T)(f: T => String, g: String => T): Draggable[T] =
    val serialised = Serialised(f(value), g)
    val kind       = Var(Option.empty[DraggableState.Kind])

    inline def bind(
        ev: EventProcessor[DragEvent, DragEvent],
        value: DraggableState.Kind
    ) =
      ev.mapToStrict(value) --> kind.someWriter

    val b = Seq[Mod[Element]](
      bind(
        onDragStart.map { ev =>
          ev.dataTransfer.setData("text/plain", serialised.string)

          ev
        },
        DraggableState.Kind.DragStart
      ),
      bind(onDrag, DraggableState.Kind.Drag),
      bind(onDragEnd, DraggableState.Kind.DragEnd)
    )

    new Draggable[T]:
      override def binders: Seq[Mod[Element]] = b
      override def events: EventStream[DraggableState[T]] =
        kind.signal.changes.collect { case Some(k) =>
          DraggableState(serialised, k)
        }
    end new
  end apply

  def string(value: String): Draggable[String] = apply(value)(identity, identity)
end Draggable
