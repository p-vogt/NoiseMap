//------------------------------------------------------------------------------
// <auto-generated>
//     Der Code wurde von einer Vorlage generiert.
//
//     Manuelle Änderungen an dieser Datei führen möglicherweise zu unerwartetem Verhalten der Anwendung.
//     Manuelle Änderungen an dieser Datei werden überschrieben, wenn der Code neu generiert wird.
// </auto-generated>
//------------------------------------------------------------------------------

namespace NoiseMapRestAPI.Models
{
    using System;
    using System.Collections.Generic;
    
    public partial class NOISE_SAMPLE
    {
        public int Id { get; set; }
        public Nullable<System.DateTime> timestamp { get; set; }
        public Nullable<double> noiseValue { get; set; }
        public Nullable<double> longitude { get; set; }
        public Nullable<double> latitude { get; set; }
        public Nullable<double> accuracy { get; set; }
        public string version { get; set; }
        public Nullable<System.DateTime> createdAt { get; set; }
        public Nullable<System.DateTime> updatedAt { get; set; }
        public Nullable<bool> deleted { get; set; }
        public Nullable<double> speed { get; set; }
        public string userName { get; set; }
    }
}
