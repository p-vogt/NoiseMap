"use strict";
const Connection = require('tedious').Connection;
const Request = require('tedious').Request;

module.exports = class DatabaseConnection {

    constructor(password) {
        this.password = password
    }
    disconnect() {
        this.connection.close();
    }
    async connect() {
        return new Promise((resolve) => {

            // Create connection to database
            var config =
            {
                userName: 'node',
                password: this.password,
                server: 'noisemap.database.windows.net',
                options:
                {
                    database: 'noisemap',
                    encrypt: true
                }
            }

            this.connection = new Connection(config);
            // Attempt to connect and execute queries if connection goes through
            this.connection.on('connect', (err) => {
                if (err) {
                    console.error(err)
                }
                resolve(err);
            });
            this.connection.on('end', (err) => {
                //Disconnected from db
            });
        });
    }

    async querySamples(longitudeStart, longitudeEnd, latitudeStart, latitudeEnd, startTime, endTime) {
        return new Promise((resolve) => {
            let result = [];
            if (startTime.indexOf(":") < 0 || endTime.indexOf(":") < 0) {
                resolve([]);
            }
            let timeFilter = "";
            const startTimeSplitted = startTime.split(":");
            const endTimeSplitted = endTime.split(":");
            const start = {
                hour: startTimeSplitted[0],
                minute: startTimeSplitted[1]
            }
            const end = {
                hour: endTimeSplitted[0],
                minute: endTimeSplitted[1]
            }
            if(start.hour === end.hour && start.minute === end.minute) {
                timeFilter ="1=1" // always true
            }
            else if (start.hour !== end.hour && start.hour < end.hour) {
                timeFilter = `( 
                    DATEPART(HOUR, timestamp) = ${start.hour} AND DATEPART(MINUTE, timestamp) >= ${start.minute}
                OR
                    DATEPART(HOUR, timestamp) > ${start.hour} AND DATEPART(HOUR, timestamp) < ${end.hour}
                OR     
                    DATEPART(HOUR, timestamp) = ${end.hour} AND DATEPART(MINUTE, timestamp) <= ${end.minute}
                )`
            } else if (start.hour === end.hour) {
                timeFilter = `(DATEPART(HOUR, timestamp) = ${start.hour} AND DATEPART(MINUTE, timestamp) >= ${start.minute} AND DATEPART(MINUTE, timestamp) <= ${end.minute})`
            } else { //start > end 
                timeFilter = `(        
                    DATEPART(HOUR, timestamp) = ${end.hour} AND DATEPART(MINUTE, timestamp) <= ${end.minute}
                OR
                    DATEPART(HOUR, timestamp) <= ${end.hour}
                OR
                    DATEPART(HOUR, timestamp) = ${start.hour} AND DATEPART(MINUTE, timestamp) >= ${start.minute}
                OR
                    DATEPART(HOUR, timestamp) > ${start.hour}
                )`
            }
            const locationFilter = `(longitude BETWEEN ${longitudeStart} AND ${longitudeEnd} AND latitude BETWEEN ${latitudeStart} AND ${latitudeEnd})`;
            const query = `SELECT timestamp, noiseValue, longitude, latitude FROM NOISE_SAMPLE WHERE ${locationFilter} AND ${timeFilter};`;
            // Read all rows from table
            const request = new Request(
                query,
                (err, rowCount, rows) => { }
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
                (err, rowCount, rows) => { }
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

        if (storedSubKeyString === '') {
            return false
        }

        const nodeCrypto = crypto.pbkdf2Sync(password, new Buffer(saltString, 'hex'), 1000, 256, 'sha1');

        // get a hex string of the derived bytes
        const derivedKeyOctets = nodeCrypto.toString('hex').toUpperCase();
        // The first 64 bytes of the derived key should
        // match the stored sub key
        const passwordsDoMatch = derivedKeyOctets.indexOf(storedSubKeyString) === 0;
        if (!passwordsDoMatch) {
            console.error("passwords do not match!");
        }
        return passwordsDoMatch;
    }

    async executeQuery(query) {
        return new Promise((resolve) => {
            let result = [];
            // Read all rows from table
            const request = new Request(
                query,
                function (err, rowCount, rows) {
                    if (err) {
                        console.err(err)
                    }
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
