package lila.tutor

import com.softwaremill.macwire.*
import com.softwaremill.tagging.*
import scala.concurrent.duration.*

import lila.common.config
import lila.db.dsl.Coll
import lila.fishnet.{ Analyser, FishnetAwaiter }
import lila.memo.CacheApi

@Module
final class Env(
    db: lila.db.Db,
    userRepo: lila.user.UserRepo,
    gameRepo: lila.game.GameRepo,
    fishnetAnalyser: Analyser,
    fishnetAwaiter: FishnetAwaiter,
    insightApi: lila.insight.InsightApi,
    perfStatsApi: lila.insight.InsightPerfStatsApi,
    settingStore: lila.memo.SettingStore.Builder,
    cacheApi: CacheApi,
    lightUserApi: lila.user.LightUserApi
)(using
    ec: scala.concurrent.ExecutionContext,
    scheduler: akka.actor.Scheduler,
    mode: play.api.Mode,
    mat: akka.stream.Materializer
):

  private val colls = TutorColls(db(config.CollName("tutor_report")), db(config.CollName("tutor_queue")))

  lazy val nbAnalysisSetting = settingStore[Int](
    "tutorNbAnalysis",
    default = 30,
    text = "Number of fishnet analysis per tutor build".some
  ).taggedWith[NbAnalysis]

  private lazy val fishnet = wire[TutorFishnet]
  private lazy val builder = wire[TutorBuilder]
  lazy val queue           = wire[TutorQueue]

  lazy val api = wire[TutorApi]

final private class TutorColls(val report: Coll, val queue: Coll)
trait NbAnalysis
