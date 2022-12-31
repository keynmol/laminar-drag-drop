package laminar.dragdrop 

import com.raquo.airstream.core.EventStream
import org.scalajs.dom.DragEvent
import com.raquo.laminar.modifiers.EventListener
import com.raquo.laminar.api.L.*

trait DropZone[T]:
  def events: EventStream[DropZoneState[T]]
  def modifiers: Seq[Modifier[Element]]

object DropZone:
  def apply[T](f: String => T) =
    val bus = EventBus[DropZoneState[T]]()

    inline def bind(
        ev: EventProcessor[DragEvent, DragEvent],
        kind: DropZoneState.Kind
    ): EventListener[DragEvent, DropZoneState[T]] =
      ev.map { ev =>
        DropZoneState(
          Serialised(ev.dataTransfer.getData("text/plain"), f),
          kind
        )
      } --> bus

    new DropZone[T]:
      def modifiers: Seq[Modifier[Element]] = Seq(
        bind(onDragEnter.preventDefault, DropZoneState.Kind.DragEnter),
        bind(onDragOver.preventDefault, DropZoneState.Kind.DragOver),
        bind(onDrop.preventDefault, DropZoneState.Kind.Drop),
        bind(onDragLeave.preventDefault, DropZoneState.Kind.DragLeave)
      )
      def events = bus.events
    end new
  end apply
end DropZone
