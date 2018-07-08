using MQTTnet;
using MQTTnet.Protocol;
using MQTTnet.Server;
using NoiseMapRestAPI.Models;
using System;
using System.Data.SqlClient;
using System.Linq;
using System.Security.Cryptography;
using System.Threading.Tasks;

namespace NoiseMapRestAPI
{

    public class MqttServer
    {
        private readonly NoiseMapEntities db = new NoiseMapEntities();

        private IMqttServer server;
        private IMqttServerOptions options;

        public MqttServer()
        {


            // Configure MQTT server.
            options = new MqttServerOptionsBuilder()
                .WithConnectionBacklog(100)
                .WithDefaultEndpointPort(1884)
                .WithConnectionValidator(connectionValidator)
                .Build();

            server = new MqttFactory().CreateMqttServer();
        }

        private SqlConnection myConnection;
        public async Task Start()
        {
            await server.StartAsync(options);

        }
        /// <summary>
        /// Validates the user credentials/context of the incoming connection (client).
        /// </summary>
        /// <param name="context">The credential context.</param>
        private void connectionValidator(MqttConnectionValidatorContext context)
        {
            if (context.ClientId.Length < 10)
            {
                context.ReturnCode = MqttConnectReturnCode.ConnectionRefusedIdentifierRejected;
                return;
            }
            var user = db.AspNetUsers.Where((u) => u.Email == context.Username).ToList();
            if (user.Count > 0)
            {
                if (VerifyHashedPassword(user[0].PasswordHash, context.Password))
                {
                    context.ReturnCode = MqttConnectReturnCode.ConnectionAccepted;
                    return;
                }
            }

            context.ReturnCode = MqttConnectReturnCode.ConnectionRefusedBadUsernameOrPassword;

        }
        private static string HashPassword(string password)
        {
            byte[] salt;
            byte[] buffer2;
            if (password == null)
            {
                throw new ArgumentNullException("password");
            }
            using (Rfc2898DeriveBytes bytes = new Rfc2898DeriveBytes(password, 0x10, 0x3e8))
            {
                salt = bytes.Salt;
                buffer2 = bytes.GetBytes(0x20);
            }
            byte[] dst = new byte[0x31];
            Buffer.BlockCopy(salt, 0, dst, 1, 0x10);
            Buffer.BlockCopy(buffer2, 0, dst, 0x11, 0x20);
            return Convert.ToBase64String(dst);
        }
        /// <summary>
        /// https://stackoverflow.com/questions/20621950/asp-net-identity-default-password-hasher-how-does-it-work-and-is-it-secure
        /// </summary>
        /// <param name="hashedPassword"></param>
        /// <param name="password"></param>
        /// <returns></returns>
        private static bool VerifyHashedPassword(string hashedPassword, string password)
        {
            byte[] buffer4;
            if (hashedPassword == null)
            {
                return false;
            }
            if (password == null)
            {
                throw new ArgumentNullException("password");
            }
            byte[] src = Convert.FromBase64String(hashedPassword);
            if ((src.Length != 0x31) || (src[0] != 0))
            {
                return false;
            }
            byte[] dst = new byte[0x10];
            Buffer.BlockCopy(src, 1, dst, 0, 0x10);
            byte[] buffer3 = new byte[0x20];
            Buffer.BlockCopy(src, 0x11, buffer3, 0, 0x20);
            using (Rfc2898DeriveBytes bytes = new Rfc2898DeriveBytes(password, dst, 0x3e8))
            {
                buffer4 = bytes.GetBytes(0x20);
            }

            return buffer3.SequenceEqual(buffer4);
        }

        public async Task Stop()
        {
            await server.StopAsync();
        }
    }

}