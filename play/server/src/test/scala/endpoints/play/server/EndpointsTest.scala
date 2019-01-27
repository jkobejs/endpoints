package endpoints.play.server

import endpoints.algebra
import endpoints.algebra.circe.JsonFromCirceCodecTestApi
import play.api.BuiltInComponents

// not really a test, just verifies algebra compatibility
class EndpointsTestApi(val playComponents: BuiltInComponents, val digests: Map[String, String])
  extends Endpoints
    with JsonEntitiesFromCodec
    with BasicAuthentication
    with Assets
    with algebra.BasicAuthTestApi
    with algebra.EndpointsTestApi
    with algebra.JsonFromCodecTestApi
    with algebra.Assets
    with JsonFromCirceCodecTestApi