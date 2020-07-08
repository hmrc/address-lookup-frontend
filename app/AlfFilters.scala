import javax.inject.Inject
import play.api.http.DefaultHttpFilters
import uk.gov.hmrc.play.bootstrap.filters.FrontendFilters

class AlfFilters @Inject()(defaultFilters: FrontendFilters)
  extends DefaultHttpFilters(defaultFilters.filters: _*)