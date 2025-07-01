package com.retail.smart;

import com.retail.smart.service.InventoryRefillServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

public class GrpcServer {

    public static void main(String[] args) {
        try {
            // Cria o servidor na porta 9090 e registra o serviço InventoryRefill
            Server server = ServerBuilder.forPort(9090)
                    .addService(new InventoryRefillServiceImpl())
                    .build();

            server.start();
            System.out.println("gRPC Server started on port 9090");

            server.awaitTermination();

        } catch (Exception e) {
            System.err.println("Failed to start gRPC server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
