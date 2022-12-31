package laminar.dragdrop

import com.raquo.laminar.api.L.*

trait DragDrop[T]:
  def draggable(value: T): Draggable[T]
  def dropZone: DropZone[T]

object DragDrop:
  private abstract class Impl[T] extends DragDrop[T]
  def apply[T](f: T => String, g: String => T): DragDrop[T] =
    new Impl:
      def draggable(value: T) = Draggable(value)(f, g)
      def dropZone            = DropZone(g)

  def string: DragDrop[String] = apply[String](identity, identity)
