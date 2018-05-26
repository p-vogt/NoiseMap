using Microsoft.Owin;
using Microsoft.Owin.Security.OAuth;
using NoiseMapRestAPI.Models;
using NoiseMapRestAPI.Providers;
using Owin;
using System;

namespace NoiseMapRestAPI
{
    public partial class Startup
    {
        public static OAuthAuthorizationServerOptions OAuthOptions { get; private set; }

        public static string PublicClientId { get; private set; }

        // further information regarding the auth. config: "http://go.microsoft.com/fwlink/?LinkId=301864".
        public void ConfigureAuth(IAppBuilder app)
        {
            app.CreatePerOwinContext(ApplicationDbContext.Create);
            app.CreatePerOwinContext<ApplicationUserManager>(ApplicationUserManager.Create);

            // configure application for oauth
            PublicClientId = "self";
            OAuthOptions = new OAuthAuthorizationServerOptions
            {
                TokenEndpointPath = new PathString("/Token"),
                Provider = new ApplicationOAuthProvider(PublicClientId),
                AuthorizeEndpointPath = new PathString("/api/Account/ExternalLogin"),
                AccessTokenExpireTimeSpan = TimeSpan.FromDays(14),
                // TODO: set to false for production
                AllowInsecureHttp = true
            };

            // activate the usage of bearer tokens (oauth)
            app.UseOAuthBearerTokens(OAuthOptions);
        }
    }
}
