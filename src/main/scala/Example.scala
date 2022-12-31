import com.raquo.laminar.api.L.*

import laminar.dragdrop.*

object Example:
  val myApp =

    val dragDrop = DragDrop.string

    inline def Image(title: String) =
      val drag = dragDrop.draggable(title)
      p(
        img(
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
      val state  = Var(List.empty[String])
      val drop   = dragDrop.dropZone
      val addOne = state.updater[String](_ :+ _)
      div(
        className := "drop-zone",
        // add event binders
        drop.modifiers,
        // render dropped bulls
        children <-- state.signal.map { bulls =>
          bulls.map(Image(_))
        },
        // when a bull is dropped - add it to the list
        drop.events.collect {
          case DropZoneState(item, DropZoneState.Kind.Drop) =>
            item.get
        } --> addOne,
        // when any item is over the box - enable drop-zone-over class
        cls.toggle("drop-zone-over") <-- drop.events.map { s =>
          s.kind == DropZoneState.Kind.DragOver
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
    renderOnDomContentLoaded(
      org.scalajs.dom.document.getElementById("app"),
      myApp
    )
end Example
