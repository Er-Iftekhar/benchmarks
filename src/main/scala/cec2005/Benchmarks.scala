package benchmarks
package cec2005

import _root_.scala.Predef.{any2stringadd => _, _}

import scalaz.{Ordering=>_,_}
import Scalaz._
import spire.algebra._
import spire.implicits._
import spire.math.{abs,cos,exp,sin,round}

import cilib._

import shapeless._
import shapeless.ops.nat._

import benchmarks.Benchmarks._
import benchmarks.dimension._
import benchmarks.matrix._
import benchmarks.implicits._

/*
 * Based on: Problem Definitions and Evaluation Criteria for the CEC 2005
 * Special Session on Real-Parameter Optimization (May 2005)
 *
 * by P. N. Suganthan, N. Hansen, J. J. Liang, K. Deb, Y. -P. Chen, A. Auger,
 * S. Tiwari
 */

object Benchmarks {
  /*
   * F1: Shifted Sphere Function
   * x ∈ [-100, 100]D
   */
  def f1[N<:Nat,A:Ring](x: Dimension[N,A])(implicit P: F1Params[N,A]): A =
    P.params match {
      case (o, fbias) => spherical(x shift o) + fbias
    }

  /*
   * F2: Shifted Schwefel’s Problem 1.2
   * x ∈ [-100, 100]D
   */
  def f2[N<:Nat,A:Ring](x: Dimension[N,A])(implicit P: F2Params[N,A]): A =
    P.params match {
      case (o, fbias) => schwefel12(x shift o) + fbias
    }

  /*
   * F3: Shifted Rotated High Conditioned Elliptic Function
   * x ∈ [-100, 100]D
   */
  def f3[N<:Nat:GTEq2,A:Field](x: Dimension[N,A])(implicit P: F3Params[N,A]): A =
    P.params match {
      case (o, m, fbias) => {
        val z = x.shift(o).rotate(m)
        elliptic(z) + fbias
      }
    }

  /*
   * F4: Shifted Schwefel’s Problem 1.2 with Noise in Fitness
   * x ∈ [-100, 100]D
   */
  def f4[N<:Nat,A:Field:Signed](x: Dimension[N,A])(implicit P: F4Params[N,A]): RVar[A] =
    P.params match {
      case (o, fbias, noise) => noise map { n =>
        schwefel12(x shift o) * (1.0 + 0.4 * abs(n)) + fbias
      }
    }

  /*
   * F5: Schwefel’s Problem 2.6 with Global Optimum on Bounds
   * x ∈ [−100,100]D
   */
  def f5[N<:Nat,A:Field:Signed:Ordering](x: Dimension[N,A])(implicit P: F5Params[N,A]): A =
    P.params match {
      case (o, a, fbias) => {
        val b = o rotate a
        // x => {
        val z = x rotate a

        (z zip b)
          .map { case (zi, bi) => abs(zi - bi) }
          .max + fbias
        // }
      }
    }

  /*
   * F6: Shifted Rosenbrock’s Function
   * x ∈ [−100,100]D
   */
  def f6[N<:Nat:GTEq2,A:Field](x: Dimension[N,A])(implicit P: F6Params[N,A]): A =
    P.params match {
      case (o, fbias) => {
        val z = (x shift o) map { _ + 1.0 }
        rosenbrock(z) + fbias
      }
    }

  /*
   * F7: Shifted Rotated Griewank’s Function without Bounds
   */
  def f7[N<:Nat,A:Field:NRoot:Trig](x: Dimension[N,A])(implicit P: F7Params[N,A]): A =
    P.params match {
      case (o, m, fbias) => griewank(x.shift(o).rotate(m)) + fbias
    }

  /*
   * F8: Shifted Rotated Ackley’s Function with Global Optimum on Bounds
   * x ∈ [−32,32]D
   */
  def f8[N<:Nat,A:Field:NRoot:Trig](x: Dimension[N,A])(implicit P: F8Params[N,A]): A =
    P.params match {
      case (o, m, fbias) => ackley(x.shift(o).rotate(m)) + fbias
    }

  /*
   * F9: Shifted Rastrigin’s Function
   * x ∈ [−5,5]D
   */
  def f9[N<:Nat,A:Field:Trig](x: Dimension[N,A])(implicit P: F9Params[N,A]): A =
    P.params match {
      case (o, fbias) => rastrigin(x shift o) + fbias
    }

  /*
   * F10: Shifted Rotated Rastrigin’s Function
   * x ∈ [−5,5]D
   */
  def f10[N<:Nat,A:Field:Trig](x: Dimension[N,A])(implicit P: F10Params[N,A]): A =
    P.params match {
      case (o, m, fbias) => rastrigin(x.shift(o).rotate(m)) + fbias
    }

  /*
   * F11: Shifted Rotated Weierstrass Function
   * x ∈ [−0.5,0.5]D
   */
  def f11[N<:Nat,A:Field:Trig](x: Dimension[N,A])(implicit P: F11Params[N,A]): A =
    P.params match {
      case (o, m, fbias) => weierstrass(x.shift(o).rotate(m)) + fbias
    }

  /*
   * F12: Schwefel’s Problem 2.13
   * x ∈ [−π,π]D
   *
   * Note: the algorithm has been modified to avoid col/row indexing.
   * 'a' and 'b' must be row-major matrices.
   */
  def f12[N<:Nat,A:Field:Trig](x: Dimension[N,A])(implicit P: F12Params[N,A]): A =
    P.params match {
      case (alpha, a, b, fbias) => {
        val A = (a zip b) map { case (ac, bc) =>
          (alpha zip ac zip bc) mapSum { case ((ai, aci), bci) =>
            aci * sin(ai) + bci * cos(ai)
          }
        }

        val B = (a zip b) map { case (ac, bc) =>
          (x zip ac zip bc) mapSum { case ((xi, aci), bci) =>
            aci * sin(xi) + bci * cos(xi)
          }
        }

        val result = (A zip B) mapSum { case (axi, bxi) => (axi - bxi) ** 2 }

        result + fbias
      }
    }

  /*
   * F13: Shifted Expanded Griewank’s plus Rosenbrock’s Function (F8F2)
   * x ∈ [−5,5]D
   */
  def f13[N<:Nat:GTEq2:HasHead,A:Field:NRoot:Trig]
    (x: Dimension[N,A])(implicit P: F13Params[N,A]): A =
      P.params match {
        case (o, fbias) => {
          val z = x.shift(o) map { _ + 1.0 }
          val pairs = (z.toList :+ z.head)
            .pairs
            .map { case (a, b) => Sized(a, b) }

          val result = pairs mapSum { pair =>
            griewank(Sized(rosenbrock(pair)))
          }
          result + fbias
        }
      }

  /*
   * F14 Shifted Rotated Expanded Scaffer’s F6 Function
   * x ∈ [−100,100]D
   */
  def f14[N<:Nat:GTEq2:HasHead,A:Field:NRoot:Trig](x: Dimension[N,A])
    (implicit P: F14Params[N,A]): A =
      P.params match {
        case (o, m, fbias) =>
          val z = x.shift(o).rotate(m)
          (z.toList :+ z.head)
            .pairs
            .mapSum { case (a, b) => schaffer6(Sized(a, b)) } + fbias
      }

  private[this] def hybrid[N<:Nat,A:Field:Signed:Trig:Ordering]
    (o: Dimension10[Dimension[N,A]],
     m: Dimension10[Matrix[N,N,A]],
     f: Dimension10[Dimension[N,A] => A],
     λ: Dimension10[A],
     σ: Dimension10[A]
   )(implicit ev: ToInt[N]): Dimension[N,A] => A = {
    val bias = Sized(0, 100, 200, 300, 400, 500, 600, 700, 800, 900)
    val C = 2000.0
    val fmax = (f zip λ zip m) map { case ((fi, λi), mi) =>
      val temp: Dimension[N,A] = Sized.wrap(Vector.fill(ev.apply)(5.0 / λi))
      val point = temp rotate mi
      abs(fi(point))
    }
    x => {
      val zipped = (o zip m zip f zip λ zip σ zip bias zip fmax) map {
        case ((((((oi, mi), fi), λi), σi), bi), fmaxi) =>
          val zi = x.shift(oi).map(_ / λi).rotate(mi)
          (oi, mi, fi, λi, σi, bi, fmaxi, zi)
      }
      val weights = zipped map {
        case (oi, _, _, _, σi, _, _, zi) =>
          val denom = (x zip oi) mapSum { case (xk, oik) => (xk - oik) ** 2 }
          exp(-denom / (2.0 * zi.size * σi * σi))
      }

      val maxWeight = weights.max
      val w1mMaxPow = 1.0 - (maxWeight ** 10)
      val adjustedWeights = weights map { wi =>
        if (wi != maxWeight) wi * w1mMaxPow
        else wi
      }
      val wSum = adjustedWeights mapSum (xi => xi)
      // normalize the weights
      val normWeights = adjustedWeights map { _ / wSum }

      (zipped zip normWeights) mapSum {
        case ((_, _, fi, _, _, bi, fmaxi, zi), wi) =>
          wi * ((C * fi(zi) / fmaxi) + bi)
      }
    }
  }

  private[this] def hybridR[N<:Nat,A:Field:Ordering:Signed:Trig]
    (o: Dimension10[Dimension[N,A]],
     m: Dimension10[Matrix[N,N,A]],
     f: Dimension10[Dimension[N,A] => RVar[A]],
     λ: Dimension10[A],
     σ: Dimension10[A]
   )(implicit ev: ToInt[N]): Dimension[N,A] => RVar[A] = {
    val bias = Sized(0, 100, 200, 300, 400, 500, 600, 700, 800, 900)
    val C = 2000.0
    val fmax: RVar[Dimension10[A]] = (f zip λ zip m) traverse { case ((fi, λi), mi) =>
      val temp: Dimension[N,A] = Sized.wrap(Vector.fill(ev.apply)(5.0 / λi))
      val point = temp rotate mi
      fi(point).map(abs(_))
    }
    x => {
      val zipped = fmax map { fMax => (o zip m zip f zip λ zip σ zip bias zip fMax) map {
        case ((((((oi, mi), fi), λi), σi), bi), fmaxi) =>
          val zi = x.shift(oi).map(_ / λi).rotate(mi)
          (oi, mi, fi, λi, σi, bi, fmaxi, zi)
      }}
      val weights = zipped map { _.map {
        case (oi, _, _, _, σi, _, _, zi) =>
          val denom = (x zip oi) mapSum { case (xk, oik) => (xk - oik) ** 2 }
          exp(-denom / (2.0 * zi.size * σi * σi))
        }
      }

      val normWeights = for {
        ws       <- weights
        maxWeight = ws.max
        w1mMaxPow = 1.0 - (maxWeight ** 10)
        adjusted  = ws.map(wi => if (wi != maxWeight) wi * w1mMaxPow else wi)
        wSum      = adjusted mapSum (xi => xi)
      } yield adjusted map { _ / wSum }

      for {
        z  <- zipped
        nw <- normWeights
        rs <- (z zip nw) traverse {
          case ((_, _, fi, _, _, bi, fmaxi, zi), wi) =>
            fi(zi) map { fiz => wi * (C * fiz / fmaxi + bi) }
        }
      } yield rs.mapSum(xi => xi)
    }
  }

  /*
   * F15 Hybrid Composition Function
   * x ∈ [−5,5]D
   */
  def f15[N<:Nat:ToInt,A:Trig:NRoot:Signed:Ordering]
    (implicit P: F15Params[N,A], A: Field[A]): Dimension[N,A] => A =
      P.params match {
        case (o, m, fbias) => {
          val funcs =
            Sized(
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _,
              ackley[N,A] _,
              ackley[N,A] _,
              spherical[N,A] _,
              spherical[N,A] _
            )
          val λ = Sized(1.0, 1.0, 10.0, 10.0, 5.0/60.0, 5.0/60.0,
                        5.0/32.0, 5.0/32.0, 5.0/100.0, 5.0/100.0).map(A.fromDouble)
          val σ = Sized(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0).map(A.fromDouble)
          val h = hybrid[N,A](o, m, funcs, λ, σ)
          x => h(x) + fbias
        }
      }

  /*
   * F16: Rotated Version of Hybrid Composition Function F15
   * x ∈ [−5,5]D
   */
  def f16[N<:Nat:ToInt,A:NRoot:Signed:Ordering:Trig]
    (implicit P: F16Params[N,A], A: Field[A]): Dimension[N,A] => A =
      P.params match {
        case (o, m, fbias) => {
          val funcs =
            Sized(
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _,
              ackley[N,A] _,
              ackley[N,A] _,
              spherical[N,A] _,
              spherical[N,A] _
            )
          val λ = Sized(1.0, 1.0, 10.0, 10.0, 5.0/60.0, 5.0/60.0,
                        5.0/32.0, 5.0/32.0, 5.0/100.0, 5.0/100.0).map(A.fromDouble)
          val σ = Sized(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }
      }

  /*
   * F17: F16 with Noise in Fitness
   * x ∈ [−5,5]D
   */
  def f17[N<:Nat:ToInt,A:Field:NRoot:Signed:Ordering:Trig]
    (x: Dimension[N,A])(implicit P16: F16Params[N,A], P17: F17Params[A]): RVar[A] =
      (P16.params, P17.params) match {
        case ((_, _, fbias16), (fbias, noise)) =>
          noise map { n =>
            val f = f16[N,A]
            val gx = f(x) - fbias16
            gx * (1.0 + 0.2 * abs(n)) + fbias
          }
        }

  /*
   * F18: Rotated Hybrid Composition Function
   * x ∈ [−5,5]D
   */
  def f18[N<:Nat:ToInt,A:NRoot:Signed:Ordering:Trig]
    (implicit P: F18Params[N,A], A: Field[A]): Dimension[N,A] => A =
      P.params match {
        case (o, m, fbias) => {
          val funcs =
            Sized(
              ackley[N,A] _,
              ackley[N,A] _,
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              spherical[N,A] _,
              spherical[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _
            )
          val λ = Sized(2 * 5.0/32.0, 5.0/32.0, 2.0 * 1.0, 1.0, 2 * 5.0/100.0,
                        5.0/100.0, 2.0 * 10.0, 10.0, 2.0 * 5.0/60.0, 5.0/60.0).map(A.fromDouble)
          val σ = Sized(1.0, 2.0, 1.5, 1.5, 1.0, 1.0, 1.5, 1.5, 2.0, 2.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }
      }

  /*
   * F19: Rotated Hybrid Composition Function with narrow basin global optimum
   * x ∈ [−5,5]D
   */
  def f19[N<:Nat:ToInt,A:NRoot:Trig:Signed:Ordering]
    (implicit P18: F18Params[N,A], P19: F19Params[A], A: Field[A]): Dimension[N,A] => A =
      (P18.params, P19.params) match {
        case ((o, m, _), fbias) =>
          val funcs =
            Sized(
              ackley[N,A] _,
              ackley[N,A] _,
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              spherical[N,A] _,
              spherical[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _
            )
          val λ = Sized(0.1 * 5.0/32.0, 5.0/32.0, 2.0 * 1.0, 1.0, 2.0 * 5.0/100.0,
                        5.0/100.0, 2.0 * 10.0, 10.0, 2.0 * 5.0/60.0, 5.0/60.0).map(A.fromDouble)
          val σ = Sized(0.1, 2.0, 1.5, 1.5, 1.0, 1.0, 1.5, 1.5, 2.0, 2.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }

  /*
   * F20: Rotated Hybrid Composition Function with Global Optimum on the Bounds
   * x ∈ [−5,5]D
   */
  def f20[N<:Nat:ToInt,A:NRoot:Ordering:Signed:Trig]
    (implicit P18: F18Params[N,A], P20: F20Params[N,A], A: Field[A]): Dimension[N,A] => A =
      (P18.params, P20.params) match {
        case ((_, m, _), (o, fbias)) =>
          val funcs =
            Sized(
              ackley[N,A] _,
              ackley[N,A] _,
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              spherical[N,A] _,
              spherical[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _
            )
          val λ = Sized(2 * 5.0/32.0, 5.0/32.0, 2.0 * 1.0, 1.0, 2 * 5.0/100.0,
                        5.0/100.0, 2.0 * 10.0, 10.0, 2.0 * 5.0/60.0, 5.0/60.0).map(A.fromDouble)
          val σ = Sized(1.0, 2.0, 1.5, 1.5, 1.0, 1.0, 1.5, 1.5, 2.0, 2.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }

  private def expandedShafferF6[N<:Nat:GTEq2:HasHead,A:Field:NRoot:Trig]
    (x: Dimension[N,A]): A =
      (x.toList :+ x.head).pairs mapSum {
        case (a, b) => schaffer6(Sized(a, b))
      }

  private def f8f2[N<:Nat:GTEq2:HasHead,A:Field:NRoot:Trig]
    (x: Dimension[N,A]): A =
      (x.toList :+ x.head).pairs mapSum {
        case (a, b) => {
          griewank(Sized(rosenbrock(Sized(a, b))))
        }
      }

  /*
   * F21: Rotated Hybrid Composition Function
   * x ∈ [−5,5]D
   */
  def f21[N<:Nat:ToInt:GTEq2:HasHead,A:Ordering:NRoot:Signed:Trig]
    (implicit P: F21Params[N,A],A: Field[A]): Dimension[N,A] => A =
      P.params match {
        case (o, m, fbias) => {
          val funcs =
            Sized(
              expandedShafferF6[N,A] _,
              expandedShafferF6[N,A] _,
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              f8f2[N,A] _,
              f8f2[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _
            )
          val λ = Sized(5.0 * 5.0/100.0, 5.0/100.0, 5.0 * 1.0, 1.0, 5.0 * 1.0,
                        1.0, 5.0 * 10.0, 10.0, 5.0 * 5.0/200.0, 5.0/200.0).map(A.fromDouble)
          val σ = Sized(1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 2.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }
      }

  /*
   * F22: Rotated Hybrid Composition Function with High Condition Number Matrix
   * x ∈ [−5,5]D
   */
  def f22[N<:Nat:ToInt:GTEq2:HasHead,A:NRoot:Ordering:Signed:Trig]
    (implicit P: F22Params[N,A], A: Field[A]): Dimension[N,A] => A =
      P.params match {
        case (o, m, fbias) => {
          val funcs =
            Sized(
              expandedShafferF6[N,A] _,
              expandedShafferF6[N,A] _,
              rastrigin[N,A] _,
              rastrigin[N,A] _,
              f8f2[N,A] _,
              f8f2[N,A] _,
              weierstrass[N,A] _,
              weierstrass[N,A] _,
              griewank[N,A] _,
              griewank[N,A] _
            )
          val λ = Sized(5.0 * 5.0/100.0, 5.0/100.0, 5.0 * 1.0, 1.0, 5.0 * 1.0,
                        1.0, 5.0 * 10.0, 10.0, 5.0 * 5.0/200.0, 5.0/200.0).map(A.fromDouble)
          val σ = Sized(1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 2.0, 2.0, 2.0, 2.0).map(A.fromDouble)
          val h = hybrid(o, m, funcs, λ, σ)

          x => h(x) + fbias
        }
      }

  /*
   * F23: Non-Continuous Rotated Hybrid Composition Function
   * x ∈ [−5,5]D
   */
  def f23[N<:Nat:ToInt:GTEq2:HasHead,A:IsReal:Signed:Ordering:NRoot:Trig]
    (implicit P21: F21Params[N,A], P23: F23Params[A], A: Field[A]): Dimension[N,A] => A =
      (P21.params, P23.params) match {
        case ((o, _, f21bias), fbias) => x =>
          val xModified = (x zip o.head) map { case (xj, o1j) =>
            if (abs(xj - o1j) < A.fromDouble(0.5)) xj
            else round(2.0 * xj) / 2.0
          }
          val f = f21[N,A]
          f(xModified) - f21bias + fbias
        }

  /*
   * F24: Rotated Hybrid Composition Function
   * x ∈ [−5,5]D
   */
  def f24[N<:Nat:ToInt:GTEq2:HasHead,A:IsReal:NRoot:Trig:Signed:Ordering]
    (implicit P: F24Params[N,A], A: Field[A]): Dimension[N,A] => RVar[A] =
      P.params match {
        case (o, m, fbias, noise) =>
          def preRound(x: Dimension[N,A]) =
            x map { xj =>
              if (abs(xj) < A.fromDouble(0.5)) xj
              else round(2.0 * xj) / 2.0
            }
          val funcsSeq: Vector[Dimension[N,A] => RVar[A]] =
            Vector(
              x => RVar.point(weierstrass[N,A](x)),
              x => RVar.point(expandedShafferF6[N,A](x)),
              x => RVar.point(f8f2[N,A](x)),
              x => RVar.point(ackley[N,A](x)),
              x => RVar.point(rastrigin[N,A](x)),
              x => RVar.point(griewank[N,A](x)),
              x => RVar.point(expandedShafferF6[N,A](preRound(x))),
              x => RVar.point(rastrigin[N,A](preRound(x))),
              x => RVar.point(elliptic(x)),
              x => noise.map { n => spherical(x) * (1.0 + 0.1 * n) }
            )
          val funcs: Dimension10[Dimension[N,A] => RVar[A]] = Sized.wrap(funcsSeq)
          val λ = Sized(10.0, 5.0/20.0, 1.0, 5.0/32.0, 1.0, 5.0/100.0,
                        5.0/50.0, 1.0, 5.0/100.0, 5.0/100.0).map(A.fromDouble)
          val σ = Sized(2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0).map(A.fromDouble)
          val h = hybridR(o, m, funcs, λ, σ)
          x => h(x) map { _ + fbias }
        }

  /*
   * F25: Rotated Hybrid Composition Function without bounds
   * x ∈ [2, 5]D
   */
  def f25[N<:Nat:ToInt:GTEq2:HasHead,A:Field:IsReal:Ordering:NRoot:Signed:Trig]
    (implicit P24: F24Params[N,A],
      P25: F25Params[A]): Dimension[N,A] => RVar[A] =
        (P24.params, P25.params) match {
          case ((_, _, f24bias, _), fbias) =>
            val f = f24[N,A]
            x => f(x) map { _ - f24bias + fbias }
          }
}
