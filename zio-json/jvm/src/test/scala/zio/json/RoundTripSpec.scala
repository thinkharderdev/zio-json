package testzio.json

import testzio.json.TestUtils._
import zio.json._
import zio.json.ast.Json
import zio.random.Random
import zio.test.Assertion._
import zio.test._

import java.time._

object RoundTripSpec extends DefaultRunnableSpec {

  def spec: ZSpec[Environment, Failure] =
    suite("RoundTrip")(
      testM("booleans") {
        check(Gen.boolean)(assertRoundtrips)
      },
      testM("bytes") {
        check(Gen.anyByte)(assertRoundtrips)
      },
      testM("shorts") {
        check(Gen.anyShort)(assertRoundtrips)
      },
      testM("ints") {
        check(Gen.anyInt)(assertRoundtrips)
      },
      testM("longs") {
        check(Gen.anyLong)(assertRoundtrips)
      },
      testM("bigInts") {
        check(genBigInteger)(assertRoundtrips)
      },
      testM("floats") {
        // NaN / Infinity is tested manually, because of == semantics
        check(Gen.anyFloat.filter(java.lang.Float.isFinite))(assertRoundtrips)
      },
      testM("doubles") {
        // NaN / Infinity is tested manually, because of == semantics
        check(Gen.anyDouble.filter(java.lang.Double.isFinite))(assertRoundtrips)
      },
      testM("AST") {
        check(genAst)(assertRoundtrips)
      },
      suite("java.time")(
        testM("DayOfWeek") {
          check(genDayOfWeek)(assertRoundtrips)
        },
        testM("Duration") {
          check(genDuration)(assertRoundtrips)
        },
        testM("Instant") {
          check(genInstant)(assertRoundtrips)
        },
        testM("LocalDate") {
          check(genLocalDate)(assertRoundtrips)
        },
        testM("LocalDateTime") {
          check(genLocalDateTime)(assertRoundtrips)
        },
        testM("LocalTime") {
          check(genLocalTime)(assertRoundtrips)
        },
        testM("Month") {
          check(genMonth)(assertRoundtrips)
        },
        testM("MonthDay") {
          check(genMonthDay)(assertRoundtrips)
        },
        testM("OffsetDateTime") {
          check(genOffsetDateTime)(assertRoundtrips)
        },
        testM("OffsetTime") {
          check(genOffsetTime)(assertRoundtrips)
        },
        testM("Period") {
          check(genPeriod)(assertRoundtrips)
        },
        testM("Year") {
          check(genYear)(assertRoundtrips)
        },
        testM("YearMonth") {
          check(genYearMonth)(assertRoundtrips)
        },
        testM("ZonedDateTime") {
          check(genZonedDateTime)(assertRoundtrips)
        },
        testM("ZoneId") {
          check(genZoneId)(assertRoundtrips[ZoneId])
        },
        testM("ZoneOffset") {
          check(genZoneOffset)(assertRoundtrips[ZoneOffset])
        }
      )
    )

  lazy val genAst: Gen[Random with Sized, Json] =
    Gen.size.flatMap { size =>
      val entry = genUsAsciiString <*> genAst
      val sz    = 0 min (size - 1)
      val obj   = Gen.chunkOfN(sz)(entry).map(Json.Obj(_))
      val arr   = Gen.chunkOfN(sz)(genAst).map(Json.Arr(_))
      val boo   = Gen.boolean.map(Json.Bool(_))
      val str   = genUsAsciiString.map(Json.Str(_))
      val num   = genBigDecimal.map(Json.Num(_))
      val nul   = Gen.const(Json.Null)

      Gen.oneOf(obj, arr, boo, str, num, nul)
    }

  private def assertRoundtrips[A: JsonEncoder: JsonDecoder](a: A) =
    assert(a.toJson.fromJson[A])(isRight(equalTo(a))) &&
      assert(a.toJsonPretty.fromJson[A])(isRight(equalTo(a)))
}
