//updated by Taittinger Gabelhart for CS449 Lab 5 Malloc lab

/*-------------------------------------------------------------------
 *  Malloc Lab Starter code:
 *        single doubly-linked free block list with LIFO policy
 *        with support for coalescing adjacent free blocks
 *
 * Terminology:
 * o We will implement an explicit free list allocator.
 * o We use "next" and "previous" to refer to blocks as ordered in
 *   the free list.
 * o We use "following" and "preceding" to refer to adjacent blocks
 *   in memory.
 *-------------------------------------------------------------------- */

#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <unistd.h>

#include "memlib.h"
#include "mm.h"

/* Macros for unscaled pointer arithmetic to keep other code cleaner.
   Casting to a char* has the effect that pointer arithmetic happens at
   the byte granularity (i.e. POINTER_ADD(0x1, 1) would be 0x2).  (By
   default, incrementing a pointer in C has the effect of incrementing
   it by the size of the type to which it points (e.g. Block).)
   We cast the result to void* to force you to cast back to the
   appropriate type and ensure you don't accidentally use the resulting
   pointer as a char* implicitly.
*/
#define UNSCALED_POINTER_ADD(p, x) ((void*)((char*)(p) + (x)))
#define UNSCALED_POINTER_SUB(p, x) ((void*)((char*)(p) - (x)))


/******** FREE LIST IMPLEMENTATION ***********************************/


/* An BlockInfo contains information about a block, including the size
   as well as pointers to the next and previous blocks in the free list.
   This is similar to the "explicit free list" structure illustrated in
   the lecture slides.

   Note that the next pointer are only needed when the block is free. To
   achieve better utilization, mm_malloc should use the space for next as
   part of the space it returns.

   +--------------+
   |     size     |  <-  Block pointers in free list point here
   |              |
   |   (header)   |
   |              |
   |     prev     |
   +--------------+
   |   nextFree   |  <-  Pointers returned by mm_malloc point here
   |   prevFree   |
   +--------------+      (allocated blocks do not have a 'nextFree' field)
   |  space and   |      (this is a space optimization...)
   |   padding    |
   |     ...      |      Free blocks write their nextFree/prevFree pointers in
   |     ...      |      this space.
   +--------------+

*/
typedef struct _BlockInfo {
  // Size of the block and whether or not the block is in use or free.
  // When the size is negative, the block is currently free.
  long int size;
  // Pointer to the previous block in the list.
  struct _Block* prev;
} BlockInfo;

/* A FreeBlockInfo structure contains metadata just for free blocks.
 * When you are ready, you can improve your naive implementation by
 * using these to maintain a separate list of free blocks.
 *
 * These are "kept" in the region of memory that is normally used by
 * the program when the block is allocated. That is, since that space
 * is free anyway, we can make good use of it to improve our malloc.
 */
typedef struct _FreeBlockInfo {
  // Pointer to the next free block in the list.
  struct _Block* nextFree;
  // Pointer to the previous free block in the list.
  struct _Block* prevFree;
} FreeBlockInfo;

/* This is a structure that can serve as all kinds of nodes.
 */
typedef struct _Block {
  BlockInfo info;
  FreeBlockInfo freeNode;
} Block;

/* Pointer to the first FreeBlockInfo in the free list, the list's head. */
static Block* free_list_head = NULL;
static Block* malloc_list_tail = NULL;

static size_t heap_size = 0;

/* Size of a word on this architecture. */
#define WORD_SIZE sizeof(void*)

/* Alignment of blocks returned by mm_malloc.
 * (We need each allocation to at least be big enough for the free space
 * metadata... so let's just align by that.)  */
#define ALIGNMENT (sizeof(FreeBlockInfo))

//inserts a free block into the freeblock list
//maintains the order of the list from the smallest negative all the way up to the largest negative (implements perfect fit)
void insertBlock(Block * block_To_Insert)
{
  Block * old_Free_head = free_list_head;//grab the old head if theres not one itll return null (Sit one)
  Block* iter_Block = old_Free_head;
  Block* left_Block = NULL;
  Block* right_Block = NULL;
  if (old_Free_head == NULL)//no head yet
  {
    //no head on list give it one make sure to set the first heads next and prev to null
    free_list_head = block_To_Insert;
    free_list_head->freeNode.nextFree = NULL;
    free_list_head->freeNode.prevFree = NULL;
    return;
  }

  while (iter_Block != NULL)
  {
    if (block_To_Insert->info.size >= iter_Block->info.size)//is bigger than iterblocks size
    {//we found a block that blocktoI should go in front of time to find out where it is in the list
      //block_To_Insert->freeNode.nextFree = iter_Block;//new heads next is the old head
      //iter_Block->freeNode.prevFree = block_To_Insert;//this connects the old head to the new head
      left_Block = iter_Block->freeNode.prevFree;//grab block to left
      right_Block = iter_Block->freeNode.nextFree;//grab the block to the right
      if (iter_Block == free_list_head)//check if iter_Block was the head (Sit two)
      {
        free_list_head = block_To_Insert;
        block_To_Insert->freeNode.nextFree = iter_Block;
        iter_Block->freeNode.prevFree = block_To_Insert;
        return;//we did all we had to do if theres an old head return
      }
      //grab block to left and right for quick check
      else if (right_Block == NULL)//(Sit three) were adding node at end of freeList
      {
        iter_Block->freeNode.nextFree = block_To_Insert;//New end of the list if block_To_Insert
        block_To_Insert->freeNode.prevFree = iter_Block;
        block_To_Insert->freeNode.nextFree = right_Block;//null
        return;
      }
      else//(Sit four) adding in between two nodes
      {
        left_Block->freeNode.nextFree = block_To_Insert;
        block_To_Insert->freeNode.prevFree = left_Block;
        right_Block->freeNode.prevFree = block_To_Insert;
        return;
      }
    }//
    iter_Block = iter_Block->freeNode.nextFree;//move it forward one
  }

  if (old_Free_head != NULL)//check if theres a head in the freelist
  {
    block_To_Insert->freeNode.nextFree = old_Free_head;//new heads next is the old head
    old_Free_head->freeNode.prevFree = block_To_Insert;//this connects the old head to the new head
    free_list_head = block_To_Insert;//update the new head
    return;//we did all we had to do if theres an old head return
  }
}

//removes the passed in free block from the free list
void removeBlock(Block* block_To_Remove)
{
  //grab the right and left block to see where block_to_remove is
  Block * right_Block = block_To_Remove->freeNode.nextFree;
  Block * left_Block = block_To_Remove->freeNode.prevFree;
  if (right_Block != NULL)//the block isnt the tail so connect it to the left block
  {
    right_Block->freeNode.prevFree = left_Block;//connect the block to the right of btR to the block to the left of btr
  }
  //have to see if btr is current head
  if(block_To_Remove == free_list_head)
  {
    free_list_head = right_Block;//move the head over one to the right
  }
  else//its not the head
  {
    left_Block->freeNode.nextFree = right_Block;//connect the block to the left  of btr to the block to the right of btr 
  }
}

/* This function will have the OS allocate more space for our heap.
 *
 * It returns a pointer to that new space. That pointer will always be
 * larger than the last request and be continuous in memory.
 */
void* requestMoreSpace(size_t reqSize);

/* This function will get the first block or returns NULL if there is not
 * one.
 *
 * You can use this to start your through search for a block.
 */
Block* first_block();

/* This function will get the adjacent block or returns NULL if there is not
 * one.
 *
 * You can use this to move along your malloc list one block at a time.
 */
Block* next_block(Block* block);

/* Use this function to print a thorough listing of your heap data structures.
 */
void examine_heap();

/* Checks the heap for any issues and prints out errors as it finds them.
 *
 * Use this when you are debugging to check for consistency issues. */
int check_heap();

/*Goes through every single block in your memory 
  looking for a free space big enough.
  Returns NULL when you cannot find any available node big enough.*/
Block* searchList(size_t reqSize) {
  Block* ptrFreeBlock = first_block();//this grabs the first block in the list
  long int checkSize = -reqSize; //changes the size to negative to check
  /* ptrFreeBlock will point to the beginning of the memory heap!
  // end will point to the end of the memory heap.
  //
  // You want to go through every block until you hit the end.
  // Make sure you read the explanation for the next_block function above.
  // It should come in handy!*/
  while (ptrFreeBlock != NULL)//while the block isnt null
  {
    if(ptrFreeBlock->info.size <= checkSize)//if the size of the block is less than or equal to the size we want return this block
    {
      return ptrFreeBlock;//return the block that will hold the req size
    }//else advance freeblock again
    ptrFreeBlock = next_block(ptrFreeBlock);//advance the block by one returns null when the block passed in has no adajcent block ie its the last block in the list
  }//end of while
  //when were done with the loop we should be looking at the last block in the list
  //this function does not allocate or set the tail it only searches the list
  return NULL;
}

/* Find a free block of at least the requested size in the free list.  Returns
   NULL if no free block is large enough. */
Block* searchFreeList(size_t reqSize) {
  Block* ptrFreeBlock = free_list_head;//grab the front of the free list (the head)
  long int checkSize = -reqSize;
  if (ptrFreeBlock != NULL)
  {
    while (ptrFreeBlock != NULL)//makes sure the next isnt null
    {
      if (ptrFreeBlock->info.size <= checkSize)//if we found a size that fits
      {
        return ptrFreeBlock;//we found a free block that fits the size
      }
      ptrFreeBlock = ptrFreeBlock->freeNode.nextFree;//make sure to advance the ptr
    }
  }
  return NULL;//we couldnt find a free block so return NULL
}

// TOP-LEVEL ALLOCATOR INTERFACE ------------------------------------

/* Allocate a block of size size and return a pointer to it. If size is zero,
 * returns null.In your mm_malloc, you will need to remove the node from the free list
   and also carefully ensure the free block from a split is in the list.
 */
/*
Using default tracefiles in traces/
Measuring performance with the interval timer.
Results for mm malloc:
trace valid util ops secs Kops
0 yes 89% 5694 0.000420 13544
1 yes 90% 5848 0.000314 18624
2 yes 94% 6648 0.000571 11649
3 yes 96% 5380 0.000438 12277
4 yes 100% 14400 0.000407 35346
5 yes 88% 4800 0.000857 5598
6 yes 88% 4800 0.000935 5135
7 yes 54% 12000 0.022908 524
8 yes 47% 24000 0.077963 308
Total 83% 83570 0.104813 797
Perf index = 50 (util) + 18 (thru) = 67/100 == 100% for lab grade
correct:9
perfidx:67
Scores are from gradescope test
*/
void* mm_malloc(size_t size) {
  Block* ptrFreeBlock = NULL;
  Block* splitBlock = NULL;
  long int reqSize; 

  // Zero-size requests get NULL.
  if (size == 0) {
    return NULL;
  }
  // Determine the amount of memory we want to allocate
  reqSize = size;
  // Round up for correct alignment
  reqSize = ALIGNMENT * ((reqSize + ALIGNMENT - 1) / ALIGNMENT);
  //call search free list with reqSize
  ptrFreeBlock = searchFreeList(reqSize);
  //now we need to check if searchList returns a null
  if (ptrFreeBlock == NULL)//catch to see if searchList returned null
  { 
    ptrFreeBlock = (Block*)requestMoreSpace(reqSize + sizeof(BlockInfo));//wasnt a free block of the size so we request more space
    ptrFreeBlock->info.size = -reqSize;//add the size to the new block its negative at first bc its a newly created free block
    ptrFreeBlock->info.prev = malloc_list_tail;//points the prev to the tail
    malloc_list_tail = ptrFreeBlock;//adds the newly created block to be  the tail
    ptrFreeBlock->info.size *=-1;//flip the size to postive to show it was allocated
    return &ptrFreeBlock->freeNode;//return it
    //we dont need to split right here because RMS returns just enough space
  }//end of null if
  //we got a non null block back make it allocated then remove from free list
  //remove the block from the free list
  removeBlock(ptrFreeBlock);
  ptrFreeBlock->info.size *=-1;//flip the size to postive to show it was allocated
  //check and see if the block we got back is bigger than actually needed for the reqSize
  //at this point ptrfreeblock's size is postive ; reqSize is postive
  if (ptrFreeBlock->info.size > (reqSize + sizeof(BlockInfo) + sizeof(FreeBlockInfo)))
  {
    splitBlock = UNSCALED_POINTER_ADD(ptrFreeBlock, sizeof(BlockInfo) + reqSize);//needs to be ptrFreeblock plus the req size plus the Blockinfo that isnt accounted for , FBI is accounted for in size
    splitBlock->info.prev = ptrFreeBlock;//puts the block to the right of our current ptrFreeblock
    //to get the extra space thats not used
    splitBlock->info.size = -(ptrFreeBlock->info.size - (reqSize + sizeof(BlockInfo) ));//the split block is a free one of extra space we overused with the first allocation of freeblock 
    if (next_block(ptrFreeBlock) == NULL)//if the previous block was the tail
    {
      malloc_list_tail = splitBlock;//if the ptrblock was to the left of the tail then change the tail to point to split
    }//end of inner if
    else//block wasnt the tail make sure to connect the block to the right of ptrFreeblock to the new split block
    {
      Block * rightBlock = next_block(ptrFreeBlock);//grab the block to the right of the current ptrfreeblock
      rightBlock->info.prev = splitBlock;//if the block wasnt the tail have to connect the block to the right of ptrFreeblock to split block
    }//end of inner else
    //at the end we set the size of ptrFreeblock to just be what was requested, the block already has the blockinfo on it 
    ptrFreeBlock->info.size = reqSize;//ptrfreeblock already has block info on it
    //add split block to the free list
    insertBlock(splitBlock);
  }//end of split if
  return &ptrFreeBlock->freeNode;//block wasnt null so return the address of the allocated block
}

/*Merges the passed in block with its left and/or block if its free
When you coalesce, you will need to ensure that the logic of the free linked list remains intact.
to do this make sure to update the freeNodes next free and prev free*/
void coalesce(Block* blockInfo) 
{
  Block* nextBlock = next_block(blockInfo);//block to the right of blockInfo if free gets absorbed by blockInfo
  Block* previousBlock = blockInfo->info.prev;//block to the left of blockInfo this will absorb blockInfo if free
  Block* tmpBlock = NULL;//used to hold the return of next_block so prevs can be updated
  //if its not null and a free block
  if (nextBlock != NULL && nextBlock->info.size < 0)
  {   
    //we merge the nextBlock with blockinfo
    blockInfo->info.size += (nextBlock->info.size + -sizeof(BlockInfo));
    tmpBlock = next_block(nextBlock);//grab the block to the right of the nextBlock
    if(tmpBlock == NULL)//if we're coalescing the tail
    {
      malloc_list_tail = blockInfo;//new tail
    }
    else//its not the tail dont worry
    {
      tmpBlock->info.prev = blockInfo;//attach the next next block to block info to account for the nextBlock that got merged
    }//end of inner else
    //update the freeNodes as well
    removeBlock(nextBlock);
  }//end of next block check 
  //if the other side is not null and a free block
  if (previousBlock != NULL && previousBlock->info.size < 0)
  {
    //merge the previousblock with blockinfo that could have been merged with nextblock only need to merge (ie add the size once)
    previousBlock->info.size += (blockInfo->info.size + -sizeof(BlockInfo));//BI size is negative PB size is negative -sizeofBI is negative
    //attach prev nextFree if not null 
    tmpBlock = next_block(blockInfo);//grab the block to the right pf block info to check and see if it was tail or just a block in the chain
    if( tmpBlock == NULL)//if were colaescing the tail
    {
      malloc_list_tail = previousBlock;//update the tail
    }//end of inner if
    else//its a block in the chain grab the block to the right of it and set it to the new left 
    { 
      tmpBlock->info.prev = previousBlock;//grabbing the block to the right and pointing it to the left
    }//end of inner else
    removeBlock(blockInfo);
  }//end of previous block check
}//end of coalesce

/* Free the block referenced by ptr. Maintains the free_list_head
   In your mm_free, you will need to add a node to the head of the free list.
   */
void mm_free(void* ptr) {
  Block* blockInfo = (Block*)UNSCALED_POINTER_SUB(ptr, sizeof(BlockInfo));
  if (blockInfo != NULL && blockInfo->info.size > 0)//make sure blockinfo isnt null and size is greater than zero
  {
    blockInfo->info.size *= -1; //force the size to be "free" by turning it negative
    insertBlock(blockInfo);//now call insert to put the free block into the free list
    coalesce(blockInfo);//no need to call coalescing with a null block
  }//end of if
}//end of free

/* --------------------------PROVIDED FUNCTIONS -------------------
 You do not need to modify these, but they might be helpful to read
 over.*/

/* Get more heap space of exact size reqSize. */
void* requestMoreSpace(size_t reqSize) {
  void* ret = UNSCALED_POINTER_ADD(mem_heap_lo(), heap_size);
  heap_size += reqSize;

  void* mem_sbrk_result = mem_sbrk(reqSize);
  if ((size_t)mem_sbrk_result == -1) {
    printf("ERROR: mem_sbrk failed in requestMoreSpace\n");
    exit(0);
  }

  return ret;
}

/* Initialize the allocator. */
int mm_init() {
  free_list_head = NULL;
  malloc_list_tail = NULL;
  heap_size = 0;

  return 0;
}

/* Gets the first block in the heap or returns NULL if there is not one. */
Block* first_block() {
  Block* first = (Block*)mem_heap_lo();
  if (heap_size == 0) {
    return NULL;
  }
  return first;
}

/* Gets the adjacent block or returns NULL if there is not one. */
Block* next_block(Block* block) {
  size_t distance = (block->info.size > 0) ? block->info.size : -block->info.size;

  Block* end = (Block*)UNSCALED_POINTER_ADD(mem_heap_lo(), heap_size);
  Block* next = (Block*)UNSCALED_POINTER_ADD(block, sizeof(BlockInfo) + distance);
  if (next >= end) {
    return NULL;
  }
  return next;
}

/* Print the heap by iterating through it as an implicit free list. */
void examine_heap() {
  /* print to stderr so output isn't buffered and not output if we crash */
  Block* curr = (Block*)mem_heap_lo();
  Block* end = (Block*)UNSCALED_POINTER_ADD(mem_heap_lo(), heap_size);
  fprintf(stderr, "heap size:\t0x%lx\n", heap_size);
  fprintf(stderr, "heap start:\t%p\n", curr);
  fprintf(stderr, "heap end:\t%p\n", end);

  fprintf(stderr, "free_list_head: %p\n", (void*)free_list_head);

  fprintf(stderr, "malloc_list_tail: %p\n", (void*)malloc_list_tail);

  while(curr && curr < end) {
    /* print out common block attributes */
    fprintf(stderr, "%p: %ld\t", (void*)curr, curr->info.size);

    /* and allocated/free specific data */
    if (curr->info.size > 0) {
      fprintf(stderr, "ALLOCATED\tprev: %p\n", (void*)curr->info.prev);
    } else {
      fprintf(stderr, "FREE\tnextFree: %p, prevFree: %p, prev: %p\n", (void*)curr->freeNode.nextFree, (void*)curr->freeNode.prevFree, (void*)curr->info.prev);
    }

    curr = next_block(curr);
  }
  fprintf(stderr, "END OF HEAP\n\n");

  curr = free_list_head;
  fprintf(stderr, "Head ");
  while(curr) {
    fprintf(stderr, "-> %p ", curr);
    curr = curr->freeNode.nextFree;
  }
  fprintf(stderr, "\n");
}

/* Checks the heap data structure for consistency. */
int check_heap() {
  Block* curr = (Block*)mem_heap_lo();
  Block* end = (Block*)UNSCALED_POINTER_ADD(mem_heap_lo(), heap_size);
  Block* last = NULL;
  long int free_count = 0;

  while(curr && curr < end) {
    if (curr->info.prev != last) {
      fprintf(stderr, "check_heap: Error: previous link not correct.\n");
      examine_heap();
    }

    if (curr->info.size <= 0) {
      // Free
      free_count++;
    }

    last = curr;
    curr = next_block(curr);
  }

  curr = free_list_head;
  last = NULL;
  while(curr) {
    if (curr == last) {
      fprintf(stderr, "check_heap: Error: free list is circular.\n");
      examine_heap();
    }
    last = curr;
    curr = curr->freeNode.nextFree;
    if (free_count == 0) {
      fprintf(stderr, "check_heap: Error: free list has more items than expected.\n");
      examine_heap();
    }
    free_count--;
  }

  return 0;
}