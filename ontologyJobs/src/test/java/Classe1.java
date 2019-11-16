import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.SystemOutDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import java.io.File;
import java.io.IOException;

public class Classe1 {
    static String base = "";
    /// loading and displaying information about ontology ****************
    @SuppressWarnings("deprecation")
    public static void loadingOntology() throws OWLOntologyCreationException,
            OWLOntologyStorageException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        File file = new File("C:/Users/hp/Desktop/university.owl");
        OWLOntology localPizza = manager.loadOntologyFromOntologyDocument(file);
        System.out.println("Loaded ontology: " + localPizza);
        IRI documentIRI = manager.getOntologyDocumentIRI(localPizza);
        System.out.println(" from: " + documentIRI);
        ////***********Adding new Classes to ontology*******
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        base= "http://www.semanticweb.org/csnyulas/ontologies/2016/6/untitled-ontology-2";;
        PrefixManager pm = new DefaultPrefixManager(
                base);
        OWLEntity entity = df.getOWLEntity(EntityType.CLASS,
                IRI.create("#newclass"));
        OWLEntity entity1 = df.getOWLEntity(EntityType.CLASS,
                IRI.create("#talya"));
        ///************Adding instances ****************
        OWLEntity lili = df.getOWLEntity(EntityType.NAMED_INDIVIDUAL,
                IRI.create("#lili"));
        //****** Adding Object Property ******

        OWLEntity hasname = df.getOWLEntity(EntityType.DATA_PROPERTY,
                IRI.create("#hasname"));
        OWLEntity hasage = df.getOWLEntity(EntityType.DATA_PROPERTY,
                IRI.create("#hasage"));

        ////****************** Adding individual in a specific Classe *********
        /// ***************** example insert Lili to the classe newclass**********

        OWLClass person = df.getOWLClass(":newclass", pm);
        OWLNamedIndividual mary = df.getOWLNamedIndividual(":lili", pm);
        OWLClassAssertionAxiom classAssertion =
                df.getOWLClassAssertionAxiom(person, mary);
///*****Adding the axiom that specify that lili is a subclass of the classe             newclass******
        manager.addAxiom(localPizza, classAssertion);
        ///******Properties Assertions***************
//+++++ GET THE SUBJECT AND OBJECT++++
        OWLIndividual matthew = df.getOWLNamedIndividual(
                IRI.create(base + "#lili"));
        OWLIndividual peter = df.getOWLNamedIndividual(
                IRI.create(base + "#john"));
//++++++LINK THE SUBJECT AND OBJECT WITH THE OBJECTPPROPERTY
        OWLObjectProperty hasFather = df.getOWLObjectProperty(
                IRI.create(base + "#hasFather"));
        OWLObjectPropertyAssertionAxiom assertion =
                df.getOWLObjectPropertyAssertionAxiom(hasFather, matthew, peter);
// +++++ Adding Axiomes to the ontology +++++
        AddAxiom addAxiomChange = new AddAxiom(localPizza, assertion);
        manager.applyChange(addAxiomChange);
/// ******** Save the ontology************
        manager.saveOntology(localPizza, new SystemOutDocumentTarget());

    }
    public static void main(String[] args) throws IOException,
            OWLOntologyStorageException {
        try {
            loadingOntology();
        } catch (OWLOntologyCreationException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}