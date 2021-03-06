package br.com.zup.edu.fretes

import br.com.zup.edu.CalculaFreteRequest
import br.com.zup.edu.CalculaFreteResponse
import br.com.zup.edu.ErrorDetails
import br.com.zup.edu.FretesServiceGrpc
import com.google.protobuf.Any
import com.google.rpc.Code
import io.grpc.Status
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para request: $request")

        val cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                    .withDescription("cep deve ser informado")
                    .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep inválido")
                .augmentDescription("formato esperado deve ser 99999-999")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        // SIMULAR uma verificacao de seguranca
        if (cep.endsWith("333")) {

            //classe Status especial que reconhece erros da bean validation
            val statusProto = com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("usuario na pode acessar esse recurso")
                .addDetails(Any.pack(ErrorDetails.newBuilder()
                    .setCode(401)
                    .setMessage("token expirado")
                    .build()))
                .build()

            //converte para classe especial que recebe o status e retorna uma exception
            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        /* Logica para converter um Double to BigDecimal, aplica numero de casas decimais e converte para Double
         valor= Random.nextDouble(from = 0.0, until = 140.0)
             .toBigDecimal()
             .setScale(2, RoundingMode.UP)
             .toDouble()*/

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0)
            if ( valor > 100.0) {
                throw IllegalArgumentException("Erro desconhecido da logica de negocio")
            }
        }catch (e: Exception) {
            responseObserver?.onError(Status.INTERNAL
                .withDescription(e.message)
                .withCause(e)          // anexado ao Status de erro, mas não é enviado ao Client
                .asRuntimeException())
        }



        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver!!.onCompleted()



    }

}