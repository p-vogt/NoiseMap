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
            this.connection.on('end', (err) => {
                console.log("Disconnected from db")
            });
        });
    }
    async querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd) {
        return new Promise((resolve) => {
            let result = [];
            let query = `SELECT * FROM NOISE_SAMPLE WHERE longitude BETWEEN ${longitudeStart} ${longitudeEnd} AND latitude BETWEEN ${latitudeStart} ${latitudeEnd};`;
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
    async checkLogin(username, password) {
        return new Promise((resolve) => {
            let passwordhash = [];
            let query = `SELECT passwordhash FROM AspNetUsers WHERE email LIKE '${username}';`;
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
                passwordhash.push(columns[0].value);
            });
            request.on('requestCompleted', () => {
                if (passwordhash && passwordhash.length > 0) {
                    resolve(this.verifyHashedPassword(passwordhash[0], password))
                } else {
                    resolve(false)
                }
            });
            this.connection.execSql(request);
        })
    }
    // modified from https://stackoverflow.com/questions/28706485/javascript-how-to-generate-rfc2898derivebytes-like-c
    verifyHashedPassword(hashedPassword, password) {
        const crypto = require('crypto');

        // The value stored in [dbo].[AspNetUsers].[PasswordHash]
        const hashedPasswordBytes = new Buffer(hashedPassword, 'base64');

        const hexChar = ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"];

        let saltString = "";
        let storedSubKeyString = "";

        // build strings of octets for the salt and the stored key
        for (var i = 1; i < hashedPasswordBytes.length; i++) {
            if (i > 0 && i <= 16) {
                saltString += hexChar[(hashedPasswordBytes[i] >> 4) & 0x0f] + hexChar[hashedPasswordBytes[i] & 0x0f]
            }
            if (i > 0 && i > 16) {
                storedSubKeyString += hexChar[(hashedPasswordBytes[i] >> 4) & 0x0f] + hexChar[hashedPasswordBytes[i] & 0x0f];
            }
        }

        if (storedSubKeyString === '') { return false }

        const nodeCrypto = crypto.pbkdf2Sync(password, new Buffer(saltString, 'hex'), 1000, 256, 'sha1');

        // get a hex string of the derived bytes
        const derivedKeyOctets = nodeCrypto.toString('hex').toUpperCase();
        // The first 64 bytes of the derived key should
        // match the stored sub key
        const passwordsDoMatch = derivedKeyOctets.indexOf(storedSubKeyString) === 0;
        return passwordsDoMatch;
    }
}
