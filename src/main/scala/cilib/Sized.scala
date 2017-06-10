package cilib

import scalaz.std.option._
import scalaz.syntax.apply._
import scalaz.syntax.foldable1._
import scalaz.{Foldable,Id}

object Sized {

  type Sized1[A] = Id.Id[A]
  type Sized2[A] = (A, A)
  type Sized3[A] = (A, A, A)
  type Sized4[A] = (A, A, A, A)
  type Sized5[A] = (A, A, A, A, A)
  type Sized6[A] = (A, A, A, A, A, A)
  type Sized10[A] = (A, A, A, A, A, A, A, A, A, A)

  final case class Sized1And[F[_]: Foldable, A](a: A, rest: F[A]) {
    val toList = a :: rest.toList
    val head = a
    val tail = rest
  }
  final case class Sized2And[F[_]: Foldable, A](a: A, b: A, rest: F[A]) {
    val toList = a :: b :: rest.toList
  }

  def toSized1[F[_]: Foldable, A](x: F[A]): Option[Sized1[A]] = x.index(0)

  def toSized2[F[_]: Foldable, A](x: F[A]): Option[Sized2[A]] =
    (x.index(0) |@| x.index(1)) { (_, _) }

  def toSized3[F[_]: Foldable, A](x: F[A]): Option[Sized3[A]] =
    (x.index(0) |@| x.index(1) |@| x.index(2)) { (_, _, _) }

  def toSized4[F[_]: Foldable, A](x: F[A]): Option[Sized4[A]] =
    (x.index(0) |@| x.index(1) |@| x.index(2) |@| x.index(3)) { (_, _, _, _) }

  def toSized5[F[_]: Foldable, A](x: F[A]): Option[Sized5[A]] =
    (x.index(0) |@| x.index(1) |@| x.index(2) |@| x.index(3) |@| x.index(4)) { (_, _, _, _, _) }

  def toSized6[F[_]: Foldable, A](x: F[A]): Option[Sized6[A]] =
    (x.index(0) |@| x.index(1) |@| x.index(2) |@| x.index(3) |@| x.index(4) |@| x.index(5)) {
      (_, _, _, _, _, _)
    }

}
