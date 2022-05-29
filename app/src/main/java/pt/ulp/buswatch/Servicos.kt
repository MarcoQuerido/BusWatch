package pt.ulp.easybus2_testes

data class Utilizadores(var firstName: String, var lastName: String, var username: String, var email: String){
    constructor(): this("","","", "")
}

data class Autocarros(var empresa: String?= null, var saida: String?= null, var chegada: String?= null,
                      var via: String?= null, var hora_partida: String?= null)

data class Partilhas(var empresa: String?= null, var saida: String?= null,
                     var chegada: String?= null, var via: String?= null,
                     var hora_partida: String?= null, var loc1Lat: Double?= null,
                     var loc1Lng: Double?= null, var loc2Lat: Double?= null,
                     var loc2Lng: Double?= null, var user: String?= null,
                     var id: String?= null, var alert: String?= null)

data class ID(var id: String?= null)

data class Favoritos(var empresa: String? = null, var saida: String? = null, var chegada: String? = null, var via: String? = null,
                     var hora_partida: String? = null)


