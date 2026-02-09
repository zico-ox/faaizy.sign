/*===== MENU SHOW =====*/ 
const showMenu = (toggleId, navId) =>{
    const toggle = document.getElementById(toggleId),
    nav = document.getElementById(navId)

    if(toggle && nav){
        toggle.addEventListener('click', ()=>{
            nav.classList.toggle('show')
        })
    }
}
showMenu('nav-toggle','nav-menu')

/*==================== REMOVE MENU MOBILE ====================*/
const navLink = document.querySelectorAll('.nav__link')

function linkAction(){
    const navMenu = document.getElementById('nav-menu')
    // When we click on each nav__link, we remove the show-menu class
    navMenu.classList.remove('show')
}
navLink.forEach(n => n.addEventListener('click', linkAction))

/*==================== SCROLL SECTIONS ACTIVE LINK ====================*/
const sections = document.querySelectorAll('section[id]')

function scrollActive(){
    const scrollY = window.pageYOffset

    sections.forEach(current =>{
        const sectionHeight = current.offsetHeight
        const sectionTop = current.offsetTop - 50;
        sectionId = current.getAttribute('id')

        if(scrollY > sectionTop && scrollY <= sectionTop + sectionHeight){
            document.querySelector('.nav__menu a[href*=' + sectionId + ']').classList.add('active')
        }else{
            document.querySelector('.nav__menu a[href*=' + sectionId + ']').classList.remove('active')
        }
    })
}
window.addEventListener('scroll', scrollActive)

/*===== SCROLL REVEAL ANIMATION =====*/
const sr = ScrollReveal({
    origin: 'top',
    distance: '60px',
    duration: 2000,
    delay: 200,
//     reset: true
});

sr.reveal('.home__data, .about__img, .skills__subtitle, .skills__text',{}); 
sr.reveal('.home__img, .about__subtitle, .about__text, .skills__img',{delay: 400}); 
sr.reveal('.home__social-icon',{ interval: 200}); 
sr.reveal('.skills__data, .work__img, .contact__input, .skill-card, .contact__information',{interval: 200}); 

/*==================== CONTACT FORM SUBMISSION ====================*/
const contactForm = document.querySelector('.contact__form');

if (contactForm) {
    contactForm.addEventListener('submit', function(event) {
        // Prevent the default form submission behavior
        event.preventDefault();

        // Find an existing notification element or create a new one
        let notification = contactForm.querySelector('.form-notification');
        if (!notification) {
            notification = document.createElement('div');
            notification.classList.add('form-notification');
            
            // Insert the notification before the submit button
            const submitButton = contactForm.querySelector('.contact__button');
            if (submitButton) {
                contactForm.insertBefore(notification, submitButton);
            } else {
                // Fallback: if for some reason the button isn't found, append it
                contactForm.appendChild(notification);
            }
        }

        // Set the notification message
        notification.textContent = 'Message not sent. Please try again later.';

        // Optionally, remove the notification after a few seconds
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 5000); // Remove after 5 seconds (5000 milliseconds)
    });
}
