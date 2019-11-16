package com.lucas.owlapi.ontology;

import com.google.common.collect.Multimap;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static junit.framework.Assert.assertNotNull;

public class ontologyFunctions {

    private static OWLOntologyManager manager;
    private static OWLOntology ontology;
    private static IRI iri;
    private static OWLDataFactory dataFactory;
    //private static OWLReasonerFactory reasonerFactory;
    //private static OWLReasoner reasoner;
    private static OpenlletReasonerFactory openlletReasonerFactory;
    private static OpenlletReasoner openlletReasoner;
    private static PrefixManager prefixManager;
    private static PrefixDocumentFormat prefixDocumentFormat;
    private static OWLObjectRenderer renderer;

    public static void main(String[] args) {
        //~~*~~ Start ontology ~~*~~
        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();
        renderer = new DLSyntaxObjectRenderer();
        ontology = createOntology();
        String pathIRI = "http://www.ontology.com/teste/teste.owl";
        iri = IRI.create(pathIRI);
        prefixManager = new DefaultPrefixManager(pathIRI+"#");
        prefixDocumentFormat = manager.getOntologyFormat(ontology).asPrefixOWLDocumentFormat();
        prefixDocumentFormat.setDefaultPrefix(pathIRI + "#");
        //String pizzaOntologyURL = "https://protege.stanford.edu/ontologies/pizza/pizza.owl";
        ontology = loadOntologyFile("teste.owl");
        openlletReasonerFactory = new OpenlletReasonerFactory();
        openlletReasoner = openlletReasonerFactory.createReasoner(ontology, new SimpleConfiguration());

        System.out.println(ontology);
        List<OWLLiteral> literalList = getDataProperty("saveData", "Lucas");

        //OWLClass person = createClass("Person");
        //OWLClass patient = createClass("Patient");
        //subclassOf(patient,person);
        //OWLNamedIndividual lucas = createNamedIndividual("Lucas");
        //createAxiomClassAndNamedIndividual(patient,lucas);
        //OWLNamedIndividual jorge = createNamedIndividual("Jorge");
        //createAxiomClassAndNamedIndividual(patient,jorge);
        //OWLDataProperty nameIs = createDataProperty("NameIs");
        //OWLDatatype datatypeString = dataFactory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
        //OWLLiteral literal = dataFactory.getOWLLiteral("Lucas Larcher", dataFactory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI()));
        //ontology.addAxioms(ontology, OWLDataPropertyAssertionAxiom(nameIs, lucas, l));
        //OWLDataPropertyAssertionAxiom dateAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(nameIs, lucas, literal);
        //OWLDataPropertyAssertionAxiom dateAssertion = createLiteral(nameIs,lucas,"Lucas Larcher",datatypeString);

        //reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        openlletReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        Scanner scanner = new Scanner(System.in);
        for(int i=0;i<10;i++){
            String id = scanner.next();
            String bpm = scanner.next();
            System.out.println(id+" "+bpm);
            OWLNamedIndividual individual = dataFactory.getOWLNamedIndividual(id,prefixManager);
            OWLDataProperty dataProperty = dataFactory.getOWLDataProperty("haveBPM",prefixManager);
            OWLDatatype datatypeString = dataFactory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
            OWLDatatype datatypeInt = dataFactory.getOWLDatatype(OWL2Datatype.XSD_INT.getIRI());
            if(individual == null)
                individual = createNamedIndividual(id);
            ArrayList<OWLLiteral> list = getLiteralsDataPropiertyAndIndividual(dataProperty, individual);
            if (list.size() > 0) {
                removeLiteral(dataProperty, individual, list.get(0));
            }
            createLiteral(dataProperty,individual,bpm,datatypeInt);
            openlletReasoner.prepareReasoner();
            openlletReasoner.refresh();
            List<OWLLiteral> ls = getDataProperty("saveData", id);
            List<OWLLiteral> ls1 = getDataProperty("haveBPM", id);
            if(ls.size()>0 && ls1.size()>0)
                System.out.println("save? "+ls.get(0).getLiteral()+"  BPM: "+ls1.get(0).getLiteral());

        }

        System.out.println(ontology);

        /*
        OWLDataPropertyAssertionAxiom dateAssertion2 = createLiteral(nameIs,lucas,"Lucas Tugas",datatypeString);
        System.out.println("teste2");
        System.out.println(ontology);

         */
        //OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual("Lucas",prefixManager);
        //getInitialInfoIndividual("Lucas", dataFactory, prefixDocumentFormat, reasoner, ontology);
        //getInitialInfoIndividual("Jorge", dataFactory, prefixDocumentFormat, openlletReasoner, ontology);

        //System.out.println(ontology);
    }




    public static OWLOntology createOntology() {
        OWLOntology o;
        try {
            o = manager.createOntology(iri);
            System.out.println(o);
            return o;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static OWLOntology loadOntologyFile(String pathName){
        File file = new File(pathName);
        OWLOntology o = null;
        try {
            o = manager.loadOntologyFromOntologyDocument(file);
            System.out.println(o);
            return o;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static OWLOntology loadOntologyURL(String URL) {
        manager = OWLManager.createOWLOntologyManager();
        IRI iri = IRI.create(URL);
        OWLOntology o = null;
        try {
            o = manager.loadOntology(iri);
            System.out.println(o);
            return o;
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void savaOntology(String pathName)
    {
        manager = OWLManager.createOWLOntologyManager();
        File fileout = new File(pathName);
        if(ontology != null) {
            try {
                manager.saveOntology(ontology, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(fileout));
            } catch (OWLOntologyStorageException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static OWLClass createClass(String name){
        OWLEntity entity = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(iri+"#"+name));
        OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(entity);
        manager.addAxiom(ontology,declare);
        OWLClass oClass = dataFactory.getOWLClass(name,prefixManager);
        return oClass;
    }
    public static OWLSubClassOfAxiom subclassOf(OWLClass oClass,OWLClass oSubclass) {
        OWLSubClassOfAxiom subClass = dataFactory.getOWLSubClassOfAxiom(oClass, oSubclass);
        //ontology.add(subClass);
        manager.addAxiom(ontology,subClass);
        return subClass;
    }

    public static void printSubclass() {
        //reasoner.getSubClasses(dataFactory.getOWLClass("http://www.ontology.com/teste/teste.owl#Teste3"), false).forEach(System.out::println);

    }

    public static void showClasses(OWLOntology o){
        assertNotNull(o);
        // Named classes referenced by axioms in the
        for (OWLClass cls :  o.getClassesInSignature())System.out.println(cls);
    }

    public static OWLDataProperty createDataProperty(String name) {
        OWLEntity entity = dataFactory.getOWLEntity(EntityType.DATA_PROPERTY, IRI.create(iri + "#" + name));
        OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(entity);
        manager.addAxiom(ontology, declare);
        //OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(iri + "#" + name);
        OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(name,prefixManager);
        return dataProperty;
    }

    public static OWLNamedIndividual createNamedIndividual(String name) {
        OWLEntity entity = dataFactory.getOWLEntity(EntityType.NAMED_INDIVIDUAL, IRI.create(iri + "#" + name));
        OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(entity);
        manager.addAxiom(ontology, declare);
        OWLNamedIndividual namedIndividual = dataFactory.getOWLNamedIndividual(name,prefixManager);
        return namedIndividual;
    }

    public static OWLObjectProperty createObjectPropertie(String name){
        OWLEntity entity = dataFactory.getOWLEntity(EntityType.OBJECT_PROPERTY, IRI.create(iri + "#" + name));
        OWLAxiom declare = dataFactory.getOWLDeclarationAxiom(entity);
        manager.addAxiom(ontology, declare);
        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(name, prefixManager);
        return objectProperty;
    }

    public static OWLDataPropertyAssertionAxiom createLiteral(OWLDataProperty dataProperty, OWLIndividual individual, String value, OWLDatatype datatype) {
        //dataFactory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI())
        OWLLiteral literal = dataFactory.getOWLLiteral(value, datatype);
        OWLDataPropertyAssertionAxiom dateAssertion = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, individual, literal);
        //ontology.add(dateAssertion);
        manager.addAxiom(ontology, dateAssertion);
        return dateAssertion;
    }
    //needs OWL factore be executed before
    public static void removeLiteral(OWLDataProperty dataProperty, OWLIndividual individual, OWLLiteral literal) {
        OWLDataPropertyAssertionAxiom datePropertyAssertionAxiom = dataFactory.getOWLDataPropertyAssertionAxiom(dataProperty, individual, literal);
        RemoveAxiom remove = new RemoveAxiom(ontology, datePropertyAssertionAxiom);
        manager.applyChange(remove);
    }
    public static void  createAxiomObjectPropertyAndIndividuals(OWLObjectProperty objectProperty, OWLIndividual individual1, OWLIndividual individual2) {
        OWLObjectPropertyAssertionAxiom assertion = dataFactory.getOWLObjectPropertyAssertionAxiom(objectProperty, individual1, individual2);
        //ontology.addAxiom(assertion);
        manager.addAxiom(ontology, assertion);
    }

    public static void createAxiomClassAndNamedIndividual(OWLClass oClass, OWLNamedIndividual namedIndividual){
        OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(oClass, namedIndividual);
        //ontology.addAxiom(classAssertion);
        manager.addAxiom(ontology, classAssertion);
    }

    public static void printAxioms(){
        for(OWLAxiom ax:ontology.getLogicalAxioms()) {
            System.out.println(ax);
        }
    }

    public static ArrayList<OWLLiteral> getLiteralsDataPropiertyAndIndividual(OWLDataProperty dataProperty, OWLNamedIndividual individual) {
        ArrayList<OWLLiteral> literals = new ArrayList<OWLLiteral>();
        //for (OWLLiteral literal : reasoner.getDataPropertyValues(individual, dataProperty)) {
        for (OWLLiteral literal : openlletReasoner.getDataPropertyValues(individual, dataProperty)) {
            literals.add(literal);
        }
        return literals;
    }

    public static OWLNamedIndividual getIndividual(String named,OWLDataFactory factory, PrefixDocumentFormat pm) {
        OWLNamedIndividual individual = factory.getOWLNamedIndividual(":"+named, pm);
        return individual;
    }

    public static List<OWLLiteral> getDataProperty(String nameDataProperty, String nameIndividual) {
        OWLNamedIndividual individual = getIndividual(nameIndividual, dataFactory, prefixDocumentFormat);
        OWLDataProperty dataProperty = dataFactory.getOWLDataProperty(":"+nameDataProperty, prefixDocumentFormat);
        //for (OWLLiteral literal : reasoner.getDataPropertyValues(individual, dataProperty)) {
        List<OWLLiteral> literals = new ArrayList<OWLLiteral>();
        for (OWLLiteral literal : openlletReasoner.getDataPropertyValues(individual, dataProperty)) {
            //System.out.println(nameIndividual+" has "+nameDataProperty+": " + literal.getLiteral());
            literals.add(literal);
        }
        return literals;
    }

    static void listAllDataPropertyValues(OWLNamedIndividual individual, OWLOntology ontology, OWLReasoner reasoner) {
        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
        Multimap<OWLDataPropertyExpression, OWLLiteral> assertedValues = EntitySearcher.getDataPropertyValues(individual, ontology);
        for (OWLDataProperty dataProp : ontology.getDataPropertiesInSignature(Imports.INCLUDED)) {
            //for (OWLLiteral literal : reasoner.getDataPropertyValues(individual, dataProp)) {
            for (OWLLiteral literal : openlletReasoner.getDataPropertyValues(individual, dataProp)) {
                Collection<OWLLiteral> literalSet = assertedValues.get(dataProp);
                boolean asserted = (literalSet != null && literalSet.contains(literal));
                System.out.println((asserted ? "asserted" : "inferred") + " data property for " + renderer.render(individual) + " : "
                        + renderer.render(dataProp) + " -> " + renderer.render(literal));
            }
        }
    }

    public static void getInitialInfoIndividual(String nameIndividual, OWLDataFactory factory, PrefixDocumentFormat pm, OWLReasoner reasoner, OWLOntology ontology) {
        OWLNamedIndividual individual = getIndividual(nameIndividual, factory, pm);

        //find to which classes the individual belongs

        Stream<OWLClassExpression> ac = EntitySearcher.getTypes(individual, ontology);
        List<OWLClassExpression> list = ac.collect(Collectors.toList());

        Collection<OWLClassExpression> assertedClasses = list;

        //for (OWLClass c : reasoner.getTypes(individual, false).getFlattened()) {
        for (OWLClass c : openlletReasoner.getTypes(individual, false).getFlattened()) {
            boolean asserted = assertedClasses.contains(c);
            System.out.println((asserted ? "asserted" : "inferred") + " class for "+nameIndividual+": " + renderer.render(c));
        }

        Multimap<OWLObjectPropertyExpression, OWLIndividual> assertedValues = EntitySearcher.getObjectPropertyValues(individual, ontology);
        for (OWLObjectProperty objProp : ontology.getObjectPropertiesInSignature(Imports.INCLUDED)) {
            //for (OWLNamedIndividual ind : reasoner.getObjectPropertyValues(individual, objProp).getFlattened()) {
            for (OWLNamedIndividual ind : openlletReasoner.getObjectPropertyValues(individual, objProp).getFlattened()) {
                boolean asserted = assertedValues.get(objProp).contains(ind);
                System.out.println((asserted ? "asserted" : "inferred") + " object property for "+nameIndividual+": "
                        + renderer.render(objProp) + " -> " + renderer.render(ind));
            }
        }
        listAllDataPropertyValues(individual,ontology,reasoner);

    }

}
