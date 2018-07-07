"use strict";
const Connection = require('tedious').Connection;
const Request = require('tedious').Request;

module.exports = class DatabaseConnection {

    constructor() {
    }
    disconnect() {
        this.connection.close();
    }
    async connect(password) {
        return new Promise((resolve) => {

            // Create connection to database
            var config =
            {
                userName: 'node',
                password: password,
                server: 'noisemap.database.windows.net',
                options:
                {
                    database: 'noisemap'
                    , encrypt: true
                }
            }

            this.connection = new Connection(config);
            // Attempt to connect and execute queries if connection goes through
            this.connection.on('connect', (err) => {
                if (err) {
                    console.log(err)
                }
                else {
                    console.log("Connected to db")
                }
                resolve(err);
            });
            this.connection.on('end', (err) =>  {
                console.log("Disconnected from db")
            });
        });
    }
    async querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {
        return new Promise((resolve) => {
            let result = [];
            let query = `SELECT * FROM NOISE_SAMPLE WHERE longitude BETWEEN ${longitudeStart} ${longitudeEnd} AND latitude BETWEEN ${latitudeStart} ${latitudeEnd};`;
            query = `SELECT * FROM NOISE_SAMPLE`;
            // Read all rows from table
            const request = new Request(
                query,
                function (err, rowCount, rows) {
                    console.log(rowCount + ' row(s) returned');
                }
            );
            request.on('error', (err) => {
                console.err(err);
            })
            request.on('row', (columns) => {
                let row = {};
                columns.forEach((column) => {
                    row[column.metadata.colName] = column.value;
                });
                result.push(row);
            });
            request.on('requestCompleted', () => {
               resolve(result)
            });
            this.connection.execSql(request);
        })

    }
}
