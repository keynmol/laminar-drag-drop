package laminar.dragdrop

trait Serialised[T]:
  def get: T
  def string: String

object Serialised:
  private class Impl[T](str: String, run: String => T) extends Serialised[T]:
    lazy val result           = run(str)
    def get: T                = result
    inline def string: String = str

  def apply[T](str: String, f: String => T): Serialised[T] = new Impl[T](str, f)
