import com.raquo.laminar.api.L.*
import org.scalajs.dom
import scala.scalajs.js.Date
import com.raquo.airstream.core.EventStream
import org.scalajs.dom.DragEvent

trait Serialised[T]:
  def get: T
  def string: String

object Serialised:
  private class Impl[T](str: String, run: String => T) extends Serialised[T]:
    lazy val result           = run(str)
    def get: T                = result
    inline def string: String = str

  def apply[T](str: String, f: String => T): Serialised[T] = new Impl[T](str, f)

case class DropZoneState[T](item: Serialised[T], kind: DropZoneState.Kind)
object DropZoneState:
  enum Kind:
    case DragEnter
    case DragOver
    case Drop
    case DragLeave

case class DraggableState[T](what: Serialised[T], kind: DraggableState.Kind)
object DraggableState:
  enum Kind:
    case DragStart
    case Drag
    case DragEnd

trait DropZone[T]:
  def events: EventStream[DropZoneState[T]]
  def binders: Seq[Binder[Element]]

object DropZone:
  def apply[T](f: String => T) =
    val bus = EventBus[DropZoneState[T]]()
    inline def bind(
        ev: EventProcessor[DragEvent, DragEvent],
        kind: DropZoneState.Kind
    ) =
      ev.map { ev =>
        DropZoneState(
          Serialised(ev.dataTransfer.getData("text/plain"), f),
          kind
        )
      } --> bus.writer

    new DropZone[T]:
      def binders: Seq[Binder[Element]] = Seq(
        bind(onDragEnter.preventDefault, DropZoneState.Kind.DragEnter),
        bind(onDragOver.preventDefault, DropZoneState.Kind.DragOver),
        bind(onDrop.preventDefault, DropZoneState.Kind.Drop),
        bind(onDragLeave.preventDefault, DropZoneState.Kind.DragLeave)
      )
      def events = bus.events
    end new
  end apply
end DropZone

trait Draggable[T]:
  def events: EventStream[DraggableState[T]]
  def binders: Seq[Binder[Element]]

  def isDragged: Signal[Boolean] =
    events
      .map {
        case DraggableState(_, DraggableState.Kind.DragEnd) => false
        case _                                              => true
      }
      .startWith(false)
end Draggable

object Draggable:
  def apply[T](value: T)(f: T => String, g: String => T) =
    val serialised = Serialised(f(value), g)
    val kind       = Var(Option.empty[DraggableState.Kind])

    inline def bind(
        ev: EventProcessor[DragEvent, DragEvent],
        value: DraggableState.Kind
    ) =
      ev.mapToStrict(value) --> kind.someWriter

    val b = Seq(
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
      override def binders: Seq[Binder[Element]] = b
      override def events: EventStream[DraggableState[T]] =
        kind.signal.changes.collect { case Some(k) =>
          DraggableState(serialised, k)
        }
    end new
  end apply

  def string(value: String) = apply(value)(identity, identity)
end Draggable

trait DragDrop[T]:
  def draggable(value: T): Draggable[T]
  def dropZone: DropZone[T]

object DragDrop:
  private abstract class Impl[T] extends DragDrop[T]
  def apply[T](f: T => String, g: String => T): DragDrop[T] =
    new Impl:
      def draggable(value: T) = Draggable(value)(f, g)
      def dropZone            = DropZone(g)

  def string = apply[String](identity, identity)

object Frontend:
  val myApp =

    val dragDrop = DragDrop.string

    inline def Image(title: String) =
      val drag = dragDrop.draggable(title)
      p(
        img(
          draggable := true,
          src       := "bull-svgrepo-com.svg",
          cls       := "bull-image",
          // add event binders
          drag.binders,
          // enable bull-image-dragged class when the bull is dragged
          cls.toggle("bull-image-dragged") <-- drag.isDragged
        ),
        b(title)
      )
    end Image

    inline def Box() =
      val state = Var(List.empty[String])
      val drop  = dragDrop.dropZone
      div(
        className := "drop-zone",
        // add event binders
        drop.binders,
        // render dropped bulls
        children <-- state.signal.map { bulls =>
          bulls.map(Image(_))
        },
        // when a bull is dropped - add it to the list
        drop.events.collect {
          case DropZoneState(item, DropZoneState.Kind.Drop) =>
            state.now() :+ item.get
        } --> state.writer,
        // when any item is over the box - enable drop-zone-over class
        cls.toggle("drop-zone-over") <-- drop.events.map {
          case DropZoneState(
                _,
                DropZoneState.Kind.DragOver
              ) =>
            true
          case _ => false
        }
      )
    end Box

    div(
      Image("bull1"),
      Image("bull2"),
      div(
        className := "drop-zone-container",
        Box(),
        Box()
      )
    )

  end myApp

  def main(args: Array[String]): Unit =
    documentEvents.onDomContentLoaded.foreach { _ =>
      render(dom.document.getElementById("app"), myApp)
    }(unsafeWindowOwner)
end Frontend
