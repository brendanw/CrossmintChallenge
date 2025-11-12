## Hello Reviewers!

- I misunderstood the direction about "not creating entries manually" for phase1. As the implementation is correct for phase2, I did not update phase1 to use the goal endpoint. My thinking was to use the response from the goal endpoint to test my code for generating 'SpaceItem's.
- From years of math coursework, I find working with (x,y) coordinates more intuitive than row=0, colum=1. I relied on this intuitive understanding to make sure my mapping functions work as intended via unit tests. This requires mapping from row-colum space to (x,y) space and back to row-column space in the code, but it makes the unit tests more intuitive.
- If I could control the backend endpoint, I'd suggest we send batches of coordinates to the server rather than individual calls.
- Phase2 code doesn't check for an error in calling the goal endpoint. That felt like overkill given the context of an assessment.
- I could further experiment with backoff policy and making concurrent calls to optimize performance, but it felt like overkill in the context of this code assessment.