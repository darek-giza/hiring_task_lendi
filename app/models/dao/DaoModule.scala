package models.dao

import com.google.inject.AbstractModule
import repositories.{ MarginRepository, MarginRepositoryImpl }
import services.{ MarginService, MarginServiceImpl, RangeService, RangeServiceImpl }

class DaoModule extends AbstractModule {

  override def configure() = {
    bind(classOf[MarginService])
      .to(classOf[MarginServiceImpl])
    bind(classOf[RangeService])
      .to(classOf[RangeServiceImpl])
    bind(classOf[MarginRepository])
      .to(classOf[MarginRepositoryImpl])
  }
}
