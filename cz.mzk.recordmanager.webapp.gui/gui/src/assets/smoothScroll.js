/**
 * Smooth scroll to the top of the element
 * 
 * @param	{string}	elementId
 * @return	{undefined}
 */
var smoothScrollToElement = function( elementId ) {
	$( 'body' ).animate( {
        scrollTop: $( elementId ).offset().top
    }, 1000);
};
